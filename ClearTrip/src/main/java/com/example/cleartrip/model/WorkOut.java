package com.example.cleartrip.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkOut {
    private Activities activities;
    private int startTime;
    private int endTime;
    private int slots;
}
