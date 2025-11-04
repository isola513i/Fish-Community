package sit.meetroom.meetingroomapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureInstantValidator.class)
@Documented
public @interface FutureInstant {
    String message() default "Time must be in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}