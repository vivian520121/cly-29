package com.cly.project.aspect;

import com.cly.project.annotation.OperationLog;
import com.cly.project.entity.OperationLogEntity;
import com.cly.project.mapper.OperationLogMapper;
import com.cly.project.util.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            saveLog(joinPoint, operationLog, result, exception, costTime);
        }
    }

    @Async
    public void saveLog(ProceedingJoinPoint joinPoint, OperationLog operationLog, Object result, Exception exception, long costTime) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            OperationLogEntity logEntity = new OperationLogEntity();
            logEntity.setModule(operationLog.module());
            logEntity.setOperation(operationLog.operation());
            logEntity.setBusinessType(operationLog.businessType());

            if (operationLog.businessIdIndex() >= 0 && operationLog.businessIdIndex() < joinPoint.getArgs().length) {
                Object arg = joinPoint.getArgs()[operationLog.businessIdIndex()];
                if (arg instanceof Long) {
                    logEntity.setBusinessId((Long) arg);
                } else if (arg instanceof Integer) {
                    logEntity.setBusinessId(((Integer) arg).longValue());
                }
            }

            logEntity.setMethod(method.getDeclaringClass().getName() + "." + method.getName());
            logEntity.setRequestMethod(request.getMethod());
            logEntity.setRequestUrl(request.getRequestURI());

            if (operationLog.saveRequest()) {
                try {
                    logEntity.setRequestParam(objectMapper.writeValueAsString(joinPoint.getArgs()));
                } catch (Exception e) {
                    logEntity.setRequestParam("参数序列化失败");
                }
            }

            if (operationLog.saveResponse() && result != null) {
                try {
                    String responseStr = objectMapper.writeValueAsString(result);
                    if (responseStr.length() > 5000) {
                        responseStr = responseStr.substring(0, 5000) + "...";
                    }
                    logEntity.setResponseResult(responseStr);
                } catch (Exception e) {
                    logEntity.setResponseResult("响应序列化失败");
                }
            }

            logEntity.setUserId(UserContext.getUserId());
            logEntity.setUsername(UserContext.getUsername());
            logEntity.setIpAddress(getClientIp(request));
            logEntity.setLocation(getLocation(logEntity.getIpAddress()));
            logEntity.setOs(getOs(request));
            logEntity.setBrowser(getBrowser(request));
            logEntity.setCostTime(costTime);
            logEntity.setStatus(exception == null ? 1 : 0);

            if (exception != null) {
                String errorMsg = exception.getMessage();
                if (errorMsg != null && errorMsg.length() > 2000) {
                    errorMsg = errorMsg.substring(0, 2000) + "...";
                }
                logEntity.setErrorMsg(errorMsg);
            }

            logEntity.setCreateTime(LocalDateTime.now());
            operationLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String getLocation(String ip) {
        try {
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                return "本机";
            }
            InetAddress inetAddress = InetAddress.getByName(ip);
            return inetAddress.getHostName();
        } catch (Exception e) {
            return "未知";
        }
    }

    private String getOs(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return "未知";
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac")) return "Mac";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        return "未知";
    }

    private String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return "未知";
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) return "IE";
        return "未知";
    }
}
