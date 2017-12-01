package mjtakenon.createmyjourney;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

public class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH); // 0から始まるので＋1?
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this,  year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        editText.setText(String.format("%04d",year)+"/"+String.format("%02d",month+1)+"/"+String.format("%02d",day));
    }

    public void setEditText(EditText text) {
        this.editText = text;
    }

    private EditText editText;
}