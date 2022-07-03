package cn.net.insurance.compensate.server.gateway.filter;

import cn.net.insurance.compensate.common.utils.InsuranceStringUtils;
import cn.net.insurance.compensate.common.utils.NewRequestWrapper;
import cn.net.insurance.compensate.server.gateway.config.ServerIgnoreWhiteProperties;
import cn.net.insurance.core.base.constant.ConfigConsts;
import cn.net.insurance.core.base.model.ExtraCodeEnum;
import cn.net.insurance.core.base.model.RespResult;
import cn.net.insurance.core.base.utils.CommonUtils;
import cn.net.insurance.core.base.utils.JacksonUtils;
import cn.net.insurance.core.common.utils.HttpParamUtils;
import cn.net.insurance.core.common.utils.IpUtils;
import cn.net.insurance.core.common.utils.LogWrite;
import cn.net.insurance.core.common.utils.ResponseWrapper;
import cn.net.insurance.core.encipher.soft.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
@RefreshScope
public class ServerAccessFilter implements Filter {
    //理赔私钥
    static final String privateTicket = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgbJP+uuPCQ7YeRoqFp4WQdQfrVNRJQtKWOJmRnfQ7LP6gCgYIKoEcz1UBgi2hRANCAATiNWt+6GeFy7Dn2C5BUmIU58gaDeerz9RPJN8st2Pg4tLYgAwOZ6Yn8Ssuz9tahb95HMC352phYM2pYw9MUbd2";
    //理赔公钥
    static final String publicTicket = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAE4jVrfuhnhcuw59guQVJiFOfIGg3nq8/UTyTfLLdj4OLS2IAMDmemJ/ErLs/bWoW/eRzAt+dqYWDNqWMPTFG3dg==";
    //保单公钥
    static final String orderPublicTicket = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEuQdUEgtONcfPhwY8z/b1SVWjsoqn+WUFrv3nYNMR0DkI08OfMSP5K1eii8mGT4obHzdiDuRAqKw6A0ySmu+jVQ==";
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private ServerIgnoreWhiteProperties serverIgnoreWhiteProperties;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        long start = System.currentTimeMillis();
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        ResponseWrapper response = new ResponseWrapper((HttpServletResponse) servletResponse);
        String ip = IpUtils.getRemoteAddress(httpServletRequest);
        String url = httpServletRequest.getRequestURI();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("pre_uuid", UUID.randomUUID().toString());
        paramMap.put("pre_out_ip", IpUtils.getRemoteAddress(httpServletRequest));
        paramMap.put("pre_url", url);
        String respBody = "";
        try {
            //白名单url
            if (InsuranceStringUtils.matches(url, serverIgnoreWhiteProperties.getWhites())) {
                filterChain.doFilter(httpServletRequest, servletResponse);
                long end = System.currentTimeMillis();
                paramMap.putAll(HttpParamUtils.getAllParams(httpServletRequest));
                log.info("白名单url请求返回:{}|{}|{}|{}|{}|{}", ip, url, start, end, end - start,JacksonUtils.toJson(paramMap));
                return;
            }
            if (!httpServletRequest.getMethod().contains(HttpMethod.POST.name()) || !httpServletRequest.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)) {
                log.error("ServiceAccessFilter请求不合法:{}|{}|{}",url,response.getContentType(),httpServletRequest.getMethod());
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write(JacksonUtils.toJson(RespResult.fail(ExtraCodeEnum.INVALID_REQUEST)));
                response.getWriter().flush();
                long end = System.currentTimeMillis();
                threadPoolTaskExecutor.execute(new LogWrite(url, ip, paramMap, RespResult.fail(ExtraCodeEnum.INVALID_REQUEST), start, end, ""));
                return;
            }
            //其他的url
            paramMap.putAll(HttpParamUtils.getAllParams(httpServletRequest));
            String versioncode = httpServletRequest.getHeader("versioncode");
            String subsystemno = httpServletRequest.getHeader("subsystemno");
            String net = httpServletRequest.getHeader("net");
            if (CommonUtils.isNumeric(versioncode)) {
                paramMap.put("versioncode", versioncode);
            }
            if (!CommonUtils.isEmpty(subsystemno)) {
                paramMap.put("subsystemno", subsystemno);
            }
            if (!CommonUtils.isEmpty(net)) {
                paramMap.put("net", net);
            }
            RespResult<Void> validateResult = this.validate(paramMap);
            if (!validateResult.result()) {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write(JacksonUtils.toJson(validateResult));
                response.getWriter().flush();
                long end = System.currentTimeMillis();
                threadPoolTaskExecutor.execute(new LogWrite(url, ip, paramMap, validateResult, start, end, ""));
                return;
            }
            NewRequestWrapper newRequestWrapper = new NewRequestWrapper(httpServletRequest, JacksonUtils.toJson(paramMap));
            filterChain.doFilter(newRequestWrapper, response);
            if (response.getStatus() == HttpStatus.OK.value() && response.getContentType() != null && response.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)) {
                respBody = new String(response.getContent(), ConfigConsts.ENCODING);
                if (StringUtils.isBlank(respBody)) {
                    log.error("respBody为空,url:{}",url);
                    long end = System.currentTimeMillis();
                    threadPoolTaskExecutor.execute(new LogWrite(url, ip, paramMap, RespResult.fail(ExtraCodeEnum.SERVER_ERROR.code, "未知错误,respBody为空"), start, end, ""));
                    return;
                }
                RespResult respResult = JacksonUtils.toObj(respBody, RespResult.class);
                if(!respResult.result()){
                    String originErrorMsg = respResult.getErrorMsg();
                    if (StringUtils.isNotBlank(originErrorMsg) && originErrorMsg.startsWith("GLOBALEXCEPTION_")) {
                        if(originErrorMsg.contains("http://") || originErrorMsg.contains("https://") ){
                            respResult.setErrorMsg("系统异常");
                        }else{
                            String errorMsg = originErrorMsg.substring(originErrorMsg.indexOf(",") + 1); //GLOBALEXCEPTION_ip,
                            respResult.setErrorMsg(errorMsg);
                        }
                        log.info("将全局异常消息进行转换，errorCode:{}|originErrorMsg:{}", respResult.getErrorCode(), originErrorMsg);
                    }
                }else if(!CommonUtils.isEmpty(respResult.getData())){
                    String respBodyStr = null;
                    if(respResult.getData() instanceof String){
                        respBodyStr = (String) respResult.getData();
                    }else{
                        respBodyStr = JacksonUtils.toJson(respResult.getData());
                    }
                    String sm4key = paramMap.get("sm4key");
                    String hash = SM3Util.encrypt(respBodyStr);
                    String sig = ByteUtils.toHexString(SignUtil.sign(hash,SecretKeyFactory.getPrivateKey(privateTicket)));
                    respBodyStr = respBodyStr + "|" + sig;
                    Object respDateStr = SM4Util.encrypt(sm4key, respBodyStr);
                    respResult.setData(respDateStr);
                }

                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write(JacksonUtils.toJson(respResult));
                response.getWriter().flush();
                long end = System.currentTimeMillis();
                threadPoolTaskExecutor.execute(new LogWrite(url, ip, paramMap, respResult, start, end, ""));
                return;
            }else{
                log.error("ServiceAccessFilter响应异常:{}|{}|{}",url,response.getContentType(),response.getStatus());
                byte[] bytes = new ObjectMapper().writeValueAsBytes(RespResult.fail(ExtraCodeEnum.DATA_DEAL_ERROR));
                response.getResponse().getOutputStream().write(bytes);
                response.getResponse().getOutputStream().flush();
                long end = System.currentTimeMillis();
                respBody = response.getContent() == null ? respBody : new String(response.getContent(), ConfigConsts.ENCODING);
                threadPoolTaskExecutor.execute(new LogWrite(url, ip, paramMap, RespResult.fail(ExtraCodeEnum.DATA_DEAL_ERROR.code,respBody), start, end, ""));
                return;
            }
        }catch (Exception e){
            log.error("ServiceAccessFilter处理异常url:{}",url,e);
            long end = System.currentTimeMillis();
            threadPoolTaskExecutor.execute(new LogWrite(url, ip, paramMap, RespResult.fail(ExtraCodeEnum.SERVER_ERROR.code, "未知错误"), start, end, ""));
        }
    }

    public RespResult<Void> validate(Map<String, String> paramMap) throws ExecutionException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException, InvalidCipherTextException, InvalidKeyException, SignatureException {
        String sid = paramMap.get("sid");
        String ts = paramMap.get("ts");
        String sign = paramMap.get("sign");
        String params = paramMap.get("params");
        String subsystemno = paramMap.get("subsystemno");
        if (CommonUtils.isEmpty(sid)) {
            return RespResult.fail(ExtraCodeEnum.ERROR_PARAMS);
        }
        if (!CommonUtils.isNumeric(ts, 20)) {
            return RespResult.fail(ExtraCodeEnum.ERROR_PARAMS);
        }
        if (!CommonUtils.isRegex(subsystemno, "^\\d{2}$")) {
            return RespResult.fail(ExtraCodeEnum.ERROR_PARAMS);
        }
        if (!CommonUtils.isRegex(sign, "^[a-zA-Z0-9]{64}")) {
            return RespResult.fail(ExtraCodeEnum.ERROR_PARAMS);
        }
        if (CommonUtils.isEmpty(params)) {
            return RespResult.fail(ExtraCodeEnum.ERROR_PARAMS);
        }
        //判断ts是否在5分钟内,超过5分钟请求失效
        if (Math.abs(System.currentTimeMillis() - Long.valueOf(ts)) > 300000) {
            return RespResult.fail(ExtraCodeEnum.INVALID_REQUEST);
        }
        //可以通过sign实现重放攻击，代码后面补充
        String sm4Key = new String(SM2Util.decrypt(SecretKeyFactory.getPrivateKey(privateTicket), ByteUtils.fromHexString(sid)));
        if (CommonUtils.isEmpty(sm4Key)) {
            return RespResult.fail(ExtraCodeEnum.AUTH_ENCYPT_DECYPT);
        }
        String decyptStr = SM4Util.decrypt(sm4Key, params);
        if (CommonUtils.isEmpty(decyptStr)) {
            return RespResult.fail(ExtraCodeEnum.AUTH_ENCYPT_DECYPT);
        }
        int index = decyptStr.lastIndexOf("|");
        if (index <= 0) {
            return RespResult.fail(ExtraCodeEnum.ERROR_PARAMS);
        }
        String sig = decyptStr.substring(index + 1);
        String paramstr = decyptStr.substring(0, index);
        if (CommonUtils.isEmpty(sig) || CommonUtils.isEmpty(paramstr)) {
            return RespResult.fail(ExtraCodeEnum.ERROR_PARAMS);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(paramstr).append("|").append(ts);
        String hash = SM3Util.encrypt(sb.toString());
        if (!sign.equalsIgnoreCase(hash)) {
            return RespResult.fail(ExtraCodeEnum.AUTH_SIGN_IVALID);
        }
        // todo 这里动态获取系统公钥，暂时调试写死
//        SubSystemBaseInfo subSystemBaseInfo = cacheService.getSubSystemBaseInfo(subsystemno);
//        if(subSystemBaseInfo == null){
//            log.info("subSystemBaseInfo获取失败:{}", subsystemno);
//            return RespResult.fail(ExtraCodeEnum.DATA_DEAL_ERROR);
//        }
        log.info("签名值sig:{},pk:{}", sig, publicTicket);
        boolean isSuccess = SignUtil.verify(hash, ByteUtils.fromHexString(sig), SecretKeyFactory.getPublicKey(orderPublicTicket));
        if (!isSuccess) {
            return RespResult.fail(ExtraCodeEnum.AUTH_SIGN_IVALID);
        }
        Map<String, String> paramstrMap = JacksonUtils.toObj(paramstr, Map.class);
        paramMap.putAll(paramstrMap);
        paramMap.put("sm4key", sm4Key);
        return RespResult.success();
    }
}
