package org.sangonomiya.app.service;

import org.jetbrains.annotations.NotNull;
import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.entity.Permission;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.app.entity.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Dioxide.CN
 * @date 2023/2/28 14:28
 * @since 1.0
 */
@Service
public interface IUserService {
    ResponseBounce<Object> loginWithUsernameAction(String username, String password, HttpServletRequest request);

    ResponseBounce<Object> loginWithEmailAction(String email, String password, HttpServletRequest request);

    ResponseBounce<Object> loginWithPhoneAction(String phone, String code, HttpServletRequest request);

    ResponseBounce<Object> registerAction(String username, String password, String email, String phone, String code, HttpServletRequest request);

    ResponseBounce<Object> changePhoneAction(String username, String password, String newPhone, String code);

    @RequestConsistency
    ResponseBounce<Object> changeProfileAction(String username, String city, String gender, String realname, Integer portrait);

    @RequestConsistency
    ResponseBounce<Object> changePasswordByLoginUserAction(String username, String oldPassword, String newPassword);

    ResponseBounce<Object> findBackPasswordAction(String username, String newPassword, String phone, String code);

    ResponseBounce<Object> eventCodeSendBeforeUserCheckAction(String username, String phone, String action, String ticket, String randstr);

    @RequestConsistency
    @AzygoService(keyPos = {0})
    ResponseBounce<Object> eventCFCodeSendBeforeUserCheckAction(String username, String phone, String action, String token);

    ResponseBounce<Object> eventCodeSendAction(String username, String phone, String action, String ticket, String randstr);

    ResponseBounce<Object> eventCFCodeSendAction(String username, String phone, String action, String token);

    ResponseBounce<Object> defaultEventCodeSendAction(String phone, String action);

    Permission getUserPermissionGroup(String username);

    @Nullable
    @RequestConsistency
    Permission getUserPermissionGroup(UserVO user);

    @Transactional()
    ResponseBounce<Object> getUserPermissionGroupFromWhichCompany(String username);

    UserVO getUserByUsername(String username);

    UserVO getUserByPhone(String phone);

    UserVO getUserByEmail(String email);

    UserVO getUserById(int id);

    boolean isInvalidCode(String phoneAction, @NotNull String code);
}
