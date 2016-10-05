package edu.utah.cs4530.rusty.paintpalette;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * The activity where the paintPalette is shown and the user can pick a color with which to draw.
 */
public class PaintPickerActivity extends AppCompatActivity implements PaletteLayout.OnColorChangedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.RED);

        setContentView(rootLayout);

        final PaletteLayout paletteLayout = new PaletteLayout(this);
        paletteLayout.setBackgroundColor(Color.WHITE);
        rootLayout.addView(paletteLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 3));
        paletteLayout.setOnColorChangedListener(this);

        paletteLayout.addSplotch(Color.RED);
        paletteLayout.addSplotch(Color.GREEN);
        paletteLayout.addSplotch(Color.BLACK);
        paletteLayout.addSplotch(Color.BLUE);
        paletteLayout.addSplotch(Color.MAGENTA);
        paletteLayout.addSplotch(Color.CYAN);

        //add a Horizontal LinearLayout for the knobs

        LinearLayout knobLayout = new LinearLayout(this);
        knobLayout.setOrientation(LinearLayout.HORIZONTAL);

        final KnobView redKnob = new KnobView(this, 1);
        redKnob.setBackgroundColor(Color.RED);

        final KnobView greenKnob = new KnobView(this, 2);
        greenKnob.setBackgroundColor(Color.GREEN);

        final KnobView blueKnob = new KnobView(this, 3);
        blueKnob.setBackgroundColor(Color.BLUE);

        knobLayout.addView(redKnob, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        knobLayout.addView(greenKnob, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        knobLayout.addView(blueKnob, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        knobLayout.setBackgroundColor(Color.rgb(Color.red(redKnob.getColor()), Color.green(greenKnob.getColor()), Color.blue(blueKnob.getColor())));

        //add another LinearLayout for add and minus buttons
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.VERTICAL);
        buttonLayout.setBackgroundColor(Color.BLACK);
        knobLayout.addView(buttonLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Button addButton = new Button(this);
        addButton.setText("Add");
        Button minusButton = new Button(this);
        minusButton.setText("Remove");
        buttonLayout.addView(addButton);
        buttonLayout.addView(minusButton);

        rootLayout.addView(knobLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 2));

        Button returnButton = new Button(this);
        returnButton.setText("Return with selected color");

        rootLayout.addView(returnButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 2));


        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                paletteLayout.addSplotch(Color.rgb(Color.red(redKnob.getColor()), Color.green(greenKnob.getColor()), Color.blue(blueKnob.getColor())));
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        minusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                paletteLayout.removeSplotch();
            }
        });

    }

    /**
     * Sets the result of the activity to be the current color that is selected.
     * @param splotchColor is the current color selected.
     */
    @Override
    public void onColorChanged(int splotchColor) {
        Intent returnSplotchColor = new Intent();
        returnSplotchColor.putExtra(MainActivity.SPLOTCH_COLOR, splotchColor);
        setResult(RESULT_OK, returnSplotchColor);
    }
}
