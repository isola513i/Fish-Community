package sit.meetroom.meetingroomapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Instant;

public class FutureInstantValidator implements ConstraintValidator<FutureInstant, Instant> {
    @Override
    public boolean isValid(Instant value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.isAfter(Instant.now());
    }
}