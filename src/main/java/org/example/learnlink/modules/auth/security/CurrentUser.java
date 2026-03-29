package org.example.learnlink.modules.auth.security;

import java.lang.annotation.*;

/**
 * Annotation to inject the current authenticated user into controller methods.
 * 
 * Usage:
 * <pre>
 * {@code
 * @GetMapping("/profile")
 * public ResponseEntity<?> getProfile(@CurrentUser UserPrincipal user) {
 *     Long userId = user.getId();
 *     // ...
 * }
 * }
 * </pre>
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
