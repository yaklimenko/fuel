package ru.yaklimenko.fuel.dialogs;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import ru.yaklimenko.fuel.R;

/**
 * Created by Антон on 07.06.2016.
 */
public class SortByPriceDialogFragment extends DialogFragment {

    public static final String TAG = SortByPriceDialogFragment.class.getSimpleName();
    public static final String ASCENDING_SORTING_KEY = "ascendingSortingKey";

    boolean ascending = true;

    public static SortByPriceDialogFragment getInstance(boolean ascending) {
        Bundle arguments = new Bundle();
        arguments.putBoolean(ASCENDING_SORTING_KEY, ascending);
        SortByPriceDialogFragment f = new SortByPriceDialogFragment();
        f.setArguments(arguments);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            ascending = savedInstanceState.getBoolean(ASCENDING_SORTING_KEY, true);
        } else if (getArguments() != null) {
            ascending = getArguments().getBoolean(ASCENDING_SORTING_KEY, true);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setSingleChoiceItems(R.array.priceSortingOptions, ascending ? 0 : 1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ascending = which == 0;
                    }
                })
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendCancel();
                    }
                })
                .create();
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(ASCENDING_SORTING_KEY, ascending);
        super.onSaveInstanceState(outState);
    }

    private void sendResult() {
        if (getTargetFragment() == null){
            return;
        }
        Intent data = getActivity().getIntent();
        data.putExtra(ASCENDING_SORTING_KEY, ascending);
        getTargetFragment().onActivityResult(
                getTargetRequestCode(), Activity.RESULT_OK, data
        );
    }

    private void sendCancel() {
        if (getTargetFragment() == null){
            return;
        }
        Intent data = getActivity().getIntent();
        getTargetFragment().onActivityResult(
                getTargetRequestCode(), Activity.RESULT_CANCELED, data
        );
    }
}
