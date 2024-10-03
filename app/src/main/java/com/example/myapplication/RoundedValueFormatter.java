package com.example.myapplication;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class RoundedValueFormatter extends ValueFormatter {

    @Override
    public String getFormattedValue(float value) {
        // Round the value to the nearest integer
        return String.valueOf(Math.round(value));
    }
}
