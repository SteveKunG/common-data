package com.stevekung.common.data;

import java.time.*;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import com.google.common.base.Preconditions;

public final class DateConverter {
    private DateConverter() {}

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private long epochMillis;
        @Nullable
        private BiFunction<String, String, String> patternResolver;
        private String outputPattern;
        @Nullable
        private Function<LocalDate, ChronoLocalDate> outputDateFormat;
        private Locale inputLocale = Locale.ROOT;
        private Locale outputLocale = Locale.ROOT;
        private final ZoneId zone = ZoneId.systemDefault();
        private ZoneId toZone = ZoneId.systemDefault();
        @Nullable
        private LocalDateTime inputDateTime;
        @Nullable
        private UnaryOperator<LocalDateTime> modifyOutputDateTime;

        private Builder() {}

        public Builder current() {
            this.epochMillis = Instant.now().toEpochMilli();
            return this;
        }

        public Builder patternResolver(BiFunction<String, String, String> patternResolver) {
            this.patternResolver = patternResolver;
            return this;
        }

        public Builder transform(UnaryOperator<LocalDateTime> modifyInputDateTime) {
            this.modifyOutputDateTime = modifyInputDateTime;
            return this;
        }

        public Builder fromEpochMillis(long ms) {
            this.epochMillis = ms;
            return this;
        }

        public Builder from(String input, String inputPattern) {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(this.patternResolver != null ? this.patternResolver.apply(input, inputPattern) : inputPattern, this.inputLocale);
            this.inputDateTime = LocalDateTime.parse(input, inputFormatter);

            if (this.modifyOutputDateTime != null) {
                this.inputDateTime = this.modifyOutputDateTime.apply(this.inputDateTime);
            }

            ZonedDateTime zdt = this.inputDateTime.atZone(this.zone);
            this.epochMillis = zdt.toInstant().toEpochMilli();
            return this;
        }

        public Builder to(String outputPattern) {
            this.outputPattern = outputPattern;
            return this;
        }

        public Builder outputDateTo(Function<LocalDate, ChronoLocalDate> outputDateFormat) {
            this.outputDateFormat = outputDateFormat;
            return this;
        }

        public Builder fromLocale(Locale inputLocale) {
            this.inputLocale = inputLocale;
            return this;
        }

        public Builder toLocale(Locale outputLocale) {
            this.outputLocale = outputLocale;
            return this;
        }

        public Builder toZone(String zoneId) {
            this.toZone = ZoneId.of(zoneId);
            return this;
        }

        public java.sql.Date toSqlDate() {
            return new java.sql.Date(this.epochMillis);
        }

        public String format() {
            Preconditions.checkState(this.inputDateTime != null || this.epochMillis != 0L,
                    "No input provided. Call from(), current(), or fromEpochMillis() first.");
            Preconditions.checkArgument(StringUtils.isNotBlank(this.outputPattern), "Output pattern should not be null");

            if (this.inputDateTime != null) {
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(this.outputPattern, this.outputLocale).withZone(this.toZone);

                if (this.outputDateFormat != null) {
                    return outputFormatter.format(this.outputDateFormat.apply(this.inputDateTime.toLocalDate()));
                }
                return this.inputDateTime.atZone(this.toZone).format(outputFormatter);
            } else {
                return DateTimeFormatter.ofPattern(this.outputPattern, this.outputLocale)
                        .withZone(this.toZone)
                        .format(Instant.ofEpochMilli(this.epochMillis));
            }
        }
    }
}
