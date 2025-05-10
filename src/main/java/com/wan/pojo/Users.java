package com.wan.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
public class Users {
    private Integer id;
    private String name;
    private String password;

    public Users(String name,String password){
        this.name = name;
        this.password = password;
    }
}
