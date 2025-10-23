package com.example.todo.security;

import com.example.todo.model.entity.Role;
import com.example.todo.model.entity.Task;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

/**
 * @author PAQUIN Pierre
 */
@Aspect
@Component
public class SecurityAspect {

    @Around("execution(* com.example.todo.repository.TaskRepository.save(..))")
    public Object setDescriptionPrefixForAdmins(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        if(args.length > 0 && args[0] instanceof Task) {
            allowPrefixDescriptionForAdmins((Task)args[0]);
        }

        return joinPoint.proceed(args);

    }

    private void allowPrefixDescriptionForAdmins(final Task task) {
        if(task.getOwner().getRoles().contains(Role.ROLE_ADMIN) || !task.getDescription().startsWith("[Internal Use]")) {
            task.setDescription("[Internal Use] " + task.getDescription());
        }
    }
}
