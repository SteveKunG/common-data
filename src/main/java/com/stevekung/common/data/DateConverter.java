package com.stevekung.common.data;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import com.google.common.base.Preconditions;

public final class DateConverter {
    private DateConverter() {}

    public static DateConverterBuilder builder() {
        return new DateConverterBuilder();
    }

    public static final class DateConverterBuilder {
        private long epochMilli;
        @Nullable
        private BiFunction<String, String, String> inputCondition;
        private String outputPattern;
        @Nullable
        private Function<LocalDate, Temporal> outputDateFormat;
        private Locale inputLocale = Locale.ROOT;
        private Locale outputLocale = Locale.ROOT;
        private final ZoneId zone = ZoneId.systemDefault();
        private ZoneId toZone = ZoneId.systemDefault();
        @Nullable
        private LocalDateTime inputDateTime;
        @Nullable
        private UnaryOperator<LocalDateTime> modifyOutputDateTime;

        private DateConverterBuilder() {}

        public DateConverterBuilder current() {
            this.epochMilli = Instant.now().toEpochMilli();
            return this;
        }

        public DateConverterBuilder inputCondition(BiFunction<String, String, String> predicate) {
            this.inputCondition = predicate;
            return this;
        }

        public DateConverterBuilder modifyOutput(UnaryOperator<LocalDateTime> modifyInputDateTime) {
            this.modifyOutputDateTime = modifyInputDateTime;
            return this;
        }

        public DateConverterBuilder from(String input, String inputPattern) {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(this.inputCondition != null ? this.inputCondition.apply(input, inputPattern) : inputPattern, this.inputLocale);
            this.inputDateTime = LocalDateTime.parse(input, inputFormatter);

            if (this.modifyOutputDateTime != null) {
                this.inputDateTime = this.modifyOutputDateTime.apply(this.inputDateTime);
            }

            ZonedDateTime zdt = this.inputDateTime.atZone(this.zone);
            this.epochMilli = zdt.toInstant().toEpochMilli();
            return this;
        }

        public DateConverterBuilder to(String outputPattern) {
            this.outputPattern = outputPattern;
            return this;
        }

        public DateConverterBuilder outputDateTo(Function<LocalDate, Temporal> outputDateFormat) {
            this.outputDateFormat = outputDateFormat;
            return this;
        }

        public DateConverterBuilder fromLocale(Locale inputLocale) {
            this.inputLocale = inputLocale;
            return this;
        }

        public DateConverterBuilder toLocale(Locale outputLocale) {
            this.outputLocale = outputLocale;
            return this;
        }

        public DateConverterBuilder toZone(String zoneId) {
            this.toZone = ZoneId.of(zoneId);
            return this;
        }

        public java.sql.Date toSqlDate() {
            return new java.sql.Date(this.epochMilli);
        }

        @Override
        public String toString() {
            Preconditions.checkArgument(StringUtils.isNotBlank(this.outputPattern), "Output pattern should not be null");

            if (this.inputDateTime != null) {
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(this.outputPattern, this.outputLocale).withZone(this.toZone);

                if (this.outputDateFormat != null) {
                    return outputFormatter.format(this.outputDateFormat.apply(this.inputDateTime.toLocalDate()));
                }
                return this.inputDateTime.atZone(this.toZone).format(outputFormatter);
            } else {
                return new SimpleDateFormat(this.outputPattern, this.outputLocale).format(this.epochMilli);
            }
        }
    }
}
