const docx = require('docx');
const fs = require('fs');

const md = fs.readFileSync('/app/data/所有对话/主对话/neusoft-eco-boot/docs/需求规格说明书.md', 'utf8');

function parseMD(text) {
  const lines = text.split('\n');
  const blocks = [];
  let inTable = false;
  let tableRows = [];
  let inCode = false;
  let codeLines = [];
  let codeLang = '';

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];

    if (line.startsWith('```')) {
      if (inCode) {
        blocks.push({ type: 'code', lang: codeLang, content: codeLines.join('\n') });
        codeLines = [];
        inCode = false;
      } else {
        inCode = true;
        codeLang = line.slice(3).trim();
      }
      continue;
    }
    if (inCode) { codeLines.push(line); continue; }

    if (line.includes('|') && line.trim().startsWith('|')) {
      const cells = line.split('|').map(c => c.trim()).filter(c => c !== '');
      if (cells.every(c => /^[-:]+$/.test(c))) continue;
      if (!inTable) { inTable = true; tableRows = []; }
      tableRows.push(cells);
      continue;
    } else if (inTable) {
      blocks.push({ type: 'table', rows: tableRows });
      tableRows = [];
      inTable = false;
    }

    const h1 = line.match(/^# (.+)/);
    const h2 = line.match(/^## (.+)/);
    const h3 = line.match(/^### (.+)/);
    if (h1) { blocks.push({ type: 'h1', text: h1[1] }); continue; }
    if (h2) { blocks.push({ type: 'h2', text: h2[1] }); continue; }
    if (h3) { blocks.push({ type: 'h3', text: h3[1] }); continue; }

    if (/^---+$/.test(line.trim())) continue;

    const li = line.match(/^(\s*)-\s+(.+)/);
    if (li) { blocks.push({ type: 'li', text: li[2], level: Math.floor(li[1].length / 2) }); continue; }

    const oli = line.match(/^(\s*)\d+\.\s+(.+)/);
    if (oli) { blocks.push({ type: 'oli', text: oli[2], level: Math.floor(oli[1].length / 2) }); continue; }

    if (line.trim()) { blocks.push({ type: 'p', text: line.trim() }); }
  }
  if (inTable) blocks.push({ type: 'table', rows: tableRows });
  return blocks;
}

function inlineToRuns(text) {
  const parts = [];
  const regex = /\*\*(.+?)\*\*|`(.+?)`|\[(.+?)\]\((.+?)\)|([^*`\[]+)/g;
  let m;
  while ((m = regex.exec(text)) !== null) {
    if (m[1]) parts.push(new docx.TextRun({ text: m[1], bold: true, font: '宋体', size: 21 }));
    else if (m[2]) parts.push(new docx.TextRun({ text: m[2], font: 'Consolas', size: 20 }));
    else if (m[3]) parts.push(new docx.TextRun({ text: m[3], font: '宋体', size: 21, color: '0066CC', underline: {} }));
    else if (m[4]) parts.push(new docx.TextRun({ text: m[4], font: '宋体', size: 21 }));
  }
  return parts.length ? parts : [new docx.TextRun({ text, font: '宋体', size: 21 })];
}

const blocks = parseMD(md);
const children = [];

for (const b of blocks) {
  switch (b.type) {
    case 'h1':
      children.push(new docx.Paragraph({
        children: [new docx.TextRun({ text: b.text, bold: true, font: '黑体', size: 32 })],
        spacing: { line: 400, before: 240, after: 120 },
        alignment: docx.AlignmentType.CENTER
      }));
      break;
    case 'h2':
      children.push(new docx.Paragraph({
        children: [new docx.TextRun({ text: b.text, bold: true, font: '黑体', size: 28 })],
        spacing: { line: 400, before: 200, after: 100 }
      }));
      break;
    case 'h3':
      children.push(new docx.Paragraph({
        children: [new docx.TextRun({ text: b.text, bold: true, font: '宋体', size: 21 })],
        spacing: { line: 400, before: 160, after: 80 }
      }));
      break;
    case 'p':
      children.push(new docx.Paragraph({
        children: inlineToRuns(b.text),
        spacing: { line: 400 }
      }));
      break;
    case 'li':
      children.push(new docx.Paragraph({
        children: inlineToRuns('\u2022 ' + b.text),
        spacing: { line: 400 },
        indent: { left: b.level * 720 + 360 }
      }));
      break;
    case 'oli':
      children.push(new docx.Paragraph({
        children: inlineToRuns(b.text),
        spacing: { line: 400 },
        indent: { left: b.level * 720 + 360 }
      }));
      break;
    case 'code':
      b.content.split('\n').forEach(l => {
        children.push(new docx.Paragraph({
          children: [new docx.TextRun({ text: l || ' ', font: 'Consolas', size: 18 })],
          spacing: { line: 360 },
          shading: { type: docx.ShadingType.SOLID, color: 'F5F5F5' }
        }));
      });
      break;
    case 'table':
      if (b.rows.length === 0) break;
      const maxCols = Math.max(...b.rows.map(r => r.length));
      const table = new docx.Table({
        rows: b.rows.map((row, ri) => new docx.TableRow({
          children: Array.from({ length: maxCols }, (_, ci) => new docx.TableCell({
            children: [new docx.Paragraph({
              children: [new docx.TextRun({
                text: row[ci] || '',
                font: '宋体', size: 20,
                bold: ri === 0
              })],
              spacing: { line: 360 }
            })],
            shading: ri === 0 ? { type: docx.ShadingType.SOLID, color: 'E8EDF3' } : undefined,
            verticalAlign: docx.VerticalAlign.CENTER
          }))
        })),
        width: { size: 100, type: docx.WidthType.PERCENTAGE }
      });
      children.push(table);
      children.push(new docx.Paragraph({ spacing: { line: 200 } }));
      break;
  }
}

const doc = new docx.Document({
  sections: [{ properties: { page: { margin: { top: 1440, bottom: 1440, left: 1440, right: 1440 } } }, children }]
});

docx.Packer.toBuffer(doc).then(buf => {
  fs.writeFileSync('/app/data/所有对话/主对话/neusoft-eco-boot/docs/需求规格说明书.docx', buf);
  console.log('Done:', buf.length, 'bytes');
});
