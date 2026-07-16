package com.finance.dashboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AiCategorizeRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "INCOME|EXPENSE", message = "Type must be INCOME or EXPENSE")
    private String type;

    public String getDescription()      { return description; }
    public void setDescription(String v){ this.description = v; }
    public String getType()             { return type; }
    public void setType(String v)       { this.type = v; }
}
