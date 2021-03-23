package com.zzc.dao;

import com.zzc.entity.User;

public interface UserMapper {
    User getUserByUid(long uid);
}
