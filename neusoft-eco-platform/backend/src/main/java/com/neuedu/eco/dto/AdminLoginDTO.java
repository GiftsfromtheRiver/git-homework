package com.neuedu.eco.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AdminLoginDTO {

    @NotBlank(message = "管理员账号不能为空")
    private String adminCode;

    @NotBlank(message = "密码不能为空")
    private String password;
}
