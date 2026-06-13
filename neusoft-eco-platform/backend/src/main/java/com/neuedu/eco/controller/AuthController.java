package com.neuedu.eco.controller;

import com.neuedu.eco.dto.AdminLoginDTO;
import com.neuedu.eco.dto.GridLoginDTO;
import com.neuedu.eco.dto.Result;
import com.neuedu.eco.entity.GridMember;
import com.neuedu.eco.entity.SysAdmin;
import com.neuedu.eco.mapper.GridMemberMapper;
import com.neuedu.eco.mapper.SysAdminMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AuthController {

    private final SysAdminMapper sysAdminMapper;
    private final GridMemberMapper gridMemberMapper;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody AdminLoginDTO dto) {
        // 1. 查询管理员
        LambdaQueryWrapper<SysAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAdmin::getUsername, dto.getAdminCode());
        SysAdmin admin = sysAdminMapper.selectOne(wrapper);

        if (admin == null) {
            return Result.error(401, "账号不存在");
        }

        // 2. 校验密码（简化版，实际应加密）
        if (!admin.getPassword().equals(dto.getPassword())) {
            return Result.error(401, "密码错误");
        }

        // 3. 生成token（简化版，实际应用JWT）
        String token = UUID.randomUUID().toString().replace("-", "");

        // 4. 返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("adminId", admin.getId());
        data.put("adminCode", admin.getUsername());
        data.put("role", admin.getRole());

        return Result.success("登录成功", data);
    }

    /**
     * 网格员登录
     */
    @PostMapping("/grid/login")
    public Result<Map<String, Object>> gridLogin(@Valid @RequestBody GridLoginDTO dto) {
        // 1. 查询网格员
        LambdaQueryWrapper<GridMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GridMember::getUsername, dto.getUsername());
        GridMember gridMember = gridMemberMapper.selectOne(wrapper);

        if (gridMember == null) {
            return Result.error(401, "账号不存在");
        }

        if (gridMember.getStatus() != null && gridMember.getStatus() == 0) {
            return Result.error(401, "账号已被禁用");
        }

        // 2. 校验密码
        if (!gridMember.getPassword().equals(dto.getPassword())) {
            return Result.error(401, "密码错误");
        }

        // 3. 生成token
        String token = UUID.randomUUID().toString().replace("-", "");

        // 4. 返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("gridMemberId", gridMember.getId());
        data.put("username", gridMember.getUsername());
        data.put("realName", gridMember.getRealName());
        data.put("gridAddress", gridMember.getGridAddress());
        data.put("phone", gridMember.getPhone());

        return Result.success("登录成功", data);
    }
}
