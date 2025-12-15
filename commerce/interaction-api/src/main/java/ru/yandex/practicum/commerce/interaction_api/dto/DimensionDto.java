package ru.yandex.practicum.commerce.interaction_api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class DimensionDto {
    @NotNull
    @Min(1)
    Double width;
    @NotNull
    @Min(1)
    Double height;
    @NotNull
    @Min(1)
    Double depth;
}