package com.wrkbr.controller;

import com.wrkbr.domain.PlatformVO;
import com.wrkbr.email.GenerateChar;
import com.wrkbr.mapper.UserMapper;
import com.wrkbr.service.UserService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;


@Controller
@Log4j
public class CommonController {

    @Setter(onMethod_ = @Autowired)
    private UserMapper userMapper;

    @Setter(onMethod_ = @Autowired)
    private UserService userService;

    @GetMapping("/accessError")
    public String accessDenied(Authentication auth, Model model) {
        log.info("Access Denied - auth: " + auth);
        model.addAttribute("msg", "권한이 없는 사용자입니다.");
        return "exception/accessError";
    }

    @GetMapping("/customLogin")
    public String customLogin(String logout, String error, Model model, String platformId, Principal principal, HttpServletResponse response) {
        log.info("customLogin...");
        log.info("logout: " + logout);
        log.info("error: " + error);
        log.info("platformId: " + platformId);

        if (logout != null)
            model.addAttribute("logout", "You have been signed out.");

        if (error != null)
            model.addAttribute("error", "Login failed. Check your account.");

        if (platformId != null) {
            model.addAttribute("platformId", platformId);
        }

        if(principal != null){
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/html; charset=utf-8");

            try (PrintWriter writer = response.getWriter()) {

                writer.println("<script>");
                writer.println("alert('현재 로그인 상태입니다.');");
                writer.println("location = '/board/list';");
                writer.println("</script>");

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        return "login/customLogin";
    }

    @GetMapping("/customLogout")
    @PreAuthorize("isAuthenticated()")
    public String customLogoutGET() {
        log.info("custom Logout GET...");
        return "login/customLogout";
    }


    @PostMapping("/customLogout")
    @PreAuthorize("isAuthenticated()")
    public void customLogoutPOST() {
        log.info("custom Logout POST...");
    }


    // 소셜 로그인 유저 정보 AJAX
    @PostMapping(value = "/getPlatformInfo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, String>> getPlatformInfo(@RequestBody Map<Object, String> userInfo) {

        GenerateChar generateChar = new GenerateChar();

        log.info("getPlatformInfo...");
        log.info("userInfo: " + userInfo);

        Map<String, String> result = new HashMap<>();

        String prefix = null;
        PlatformVO platformVO = new PlatformVO();


        // 카카오
        if (userInfo.get("platform").equals("kakao")) {

            platformVO.setKakao_id(userInfo.get("id"));
            platformVO.setKa_generate(generateChar.generateChar());

        // 페이스북
        } else if (userInfo.get("platform").equals("facebook")) {

            platformVO.setFacebook_id(userInfo.get("id"));
            platformVO.setFb_generate(generateChar.generateChar());

        // 구글
        } else if (userInfo.get("platform").equals("google")) {

            platformVO.setGoogle_id(userInfo.get("id"));
            platformVO.setGg_generate(generateChar.generateChar());
        }

        platformVO.setUserid(userInfo.get("id"));
        prefix = insertPlatformUser(platformVO);

        result.put("prefix", prefix);

        log.info("prefix: " + prefix);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    private String insertPlatformUser(PlatformVO platformVO) {

        String prefix = null;

        prefix = userMapper.selectPrefix(platformVO);

        // prefix가 검색되지 않으면 insert하고 다시 select
        if (prefix == null) {
            userService.insertAllInfoPlatformUser(platformVO);
            prefix = userMapper.selectPrefix(platformVO);
        }

        return prefix;
    }


}





