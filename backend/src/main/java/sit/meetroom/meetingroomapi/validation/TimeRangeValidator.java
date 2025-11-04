package sit.meetroom.meetingroomapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;

public class TimeRangeValidator implements ConstraintValidator<ValidTimeRange, Object> {
    private String startField;
    private String endField;
    private int minDurationMinutes;
    private int maxDurationHours;

    @Override
    public void initialize(ValidTimeRange annotation) {
        this.startField = annotation.startField();
        this.endField = annotation.endField();
        this.minDurationMinutes = annotation.minDurationMinutes();
        this.maxDurationHours = annotation.maxDurationHours();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        try {
            Field startFieldObj = object.getClass().getDeclaredField(startField);
            Field endFieldObj = object.getClass().getDeclaredField(endField);

            startFieldObj.setAccessible(true);
            endFieldObj.setAccessible(true);

            Instant start = (Instant) startFieldObj.get(object);
            Instant end = (Instant) endFieldObj.get(object);

            if (start == null || end == null) {
                return true;
            }

            if (!end.isAfter(start)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("End time must be after start time")
                        .addPropertyNode(endField)
                        .addConstraintViolation();
                return false;
            }

            Duration duration = Duration.between(start, end);

            if (duration.toMinutes() < minDurationMinutes) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                                "Booking duration must be at least " + minDurationMinutes + " minutes")
                        .addPropertyNode(endField)
                        .addConstraintViolation();
                return false;
            }

            if (duration.toHours() > maxDurationHours) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                                "Booking duration cannot exceed " + maxDurationHours + " hours")
                        .addPropertyNode(endField)
                        .addConstraintViolation();
                return false;
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}