package com.unified.healthfitness;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class tdeecalculatorMainActivity extends AppCompatActivity {

    EditText etAge, etWeight, etHeight;
    RadioGroup genderGroup;
    Spinner spinnerActivity;
    Button btnCalculate, btnClear;
    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator_main);

        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        genderGroup = findViewById(R.id.genderGroup);
        spinnerActivity = findViewById(R.id.spActivityLevel);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnClear = findViewById(R.id.btnClear);
        tvResult = findViewById(R.id.tvResult);

        // Populate spinner using string-array from strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.activity_levels,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivity.setAdapter(adapter);

        // Calculate button
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateTDEE();
            }
        });

        // Clear button
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
            }
        });
    }

    private void calculateTDEE() {
        String ageStr = etAge.getText().toString();
        String weightStr = etWeight.getText().toString();
        String heightStr = etHeight.getText().toString();

        if (ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);
        double weight = Double.parseDouble(weightStr);
        double height = Double.parseDouble(heightStr);

        int selectedGenderId = genderGroup.getCheckedRadioButtonId();
        boolean isMale = selectedGenderId == R.id.rbMale;

        // Step 1: Calculate BMR
        double BMR;
        if (isMale) {
            BMR = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else {
            BMR = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }

        // Step 2: Get Activity Multiplier
        double multiplier;
        switch (spinnerActivity.getSelectedItemPosition()) {
            case 0:
                multiplier = 1.2;
                break;
            case 1:
                multiplier = 1.375;
                break;
            case 2:
                multiplier = 1.55;
                break;
            case 3:
                multiplier = 1.725;
                break;
            case 4:
                multiplier = 1.9;
                break;
            default:
                multiplier = 1.2;
        }

        double TDEE = BMR * multiplier;

        tvResult.setText("Your TDEE is: " + String.format("%.2f", TDEE) + " Calories/day");
    }

    private void clearFields() {
        etAge.setText("");
        etWeight.setText("");
        etHeight.setText("");
        genderGroup.check(R.id.rbMale);
        spinnerActivity.setSelection(0);
        tvResult.setText("Your TDEE result will appear here");

        Toast.makeText(this, "Fields cleared!", Toast.LENGTH_SHORT).show();
    }
}
