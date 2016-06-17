package ru.yaklimenko.fuel.dialogs;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

import ru.yaklimenko.fuel.R;
import ru.yaklimenko.fuel.db.dao.FuelCategoryDao;
import ru.yaklimenko.fuel.db.entities.FuelCategory;

/**
 * Created by Антон on 31.05.2016.
 * dialog to choose fuel type
 */
public class FilterFuelDialogFragment extends DialogFragment {
    public static final String TAG = FilterFuelDialogFragment.class.getSimpleName();
    public static final String FUEL_CATEGORY_KEY = "fuelCategoryId";
    public static final String FUEL_CATEGORY_CAN_CLEAR_CHOICE_KEY = "canClearChoice";

    /**
     * currently used on maps fuel type id
     */
    private int fuelCategoryId = -1;

    private boolean canClearChoice = true;

    public static FilterFuelDialogFragment getInstance(Integer fuelCategoryId) {
        FilterFuelDialogFragment f = new FilterFuelDialogFragment();
        if (fuelCategoryId != null) {
            Bundle args = new Bundle();
            args.putInt(FUEL_CATEGORY_KEY, fuelCategoryId);
            f.setArguments(args);
        }
        return f;
    }

    public static FilterFuelDialogFragment getInstance(Integer fuelCategoryId, boolean canClearChoice) {
        FilterFuelDialogFragment f = new FilterFuelDialogFragment();
        if (fuelCategoryId != null) {
            Bundle args = new Bundle();
            args.putInt(FUEL_CATEGORY_KEY, fuelCategoryId);
            args.putBoolean(FUEL_CATEGORY_CAN_CLEAR_CHOICE_KEY, canClearChoice);
            f.setArguments(args);
        }
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            fuelCategoryId = savedInstanceState.getInt(FUEL_CATEGORY_KEY, -1);
            canClearChoice = savedInstanceState.getBoolean(FUEL_CATEGORY_CAN_CLEAR_CHOICE_KEY, true);
        } else if (getArguments() != null){
            Bundle arguments = getArguments();
            if (arguments.containsKey(FUEL_CATEGORY_KEY)) {
                fuelCategoryId = arguments.getInt(FUEL_CATEGORY_KEY);
            }
            if (arguments.containsKey(FUEL_CATEGORY_CAN_CLEAR_CHOICE_KEY)) {
                canClearChoice = arguments.getBoolean(FUEL_CATEGORY_CAN_CLEAR_CHOICE_KEY, true);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final List<FuelCategory> categories = FuelCategoryDao.getInstance().getAllSorted();

        DialogInterface.OnClickListener selectedItemListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fuelCategoryId = categories.get(which).id;
            }
        };

        DialogInterface.OnClickListener clearClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendResult(-1);
            }
        };

        DialogInterface.OnClickListener okClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendResult(fuelCategoryId);
            }
        };

        CharSequence[] catNames = new CharSequence[categories.size()];
        int checkedItemPosition = -1;

        for (int i = 0; i < categories.size(); i++) {
            FuelCategory category = categories.get(i);
            if (fuelCategoryId == category.id) {
                checkedItemPosition = i;
            }
            catNames[i] = category.name;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_fuelfilter_title)
                .setSingleChoiceItems(catNames, checkedItemPosition, selectedItemListener)
                .setPositiveButton(R.string.dialog_ok, okClickListener)
                .setNegativeButton(R.string.dialog_cancel, null);
        if (canClearChoice) {
            builder.setNeutralButton(R.string.dialog_fuelfilter_clear_button, clearClickListener);
        }
        return builder.create();
    }

    private void sendResult(Integer fuelCategoryId) {
        if (getTargetFragment() == null){
            return;
        }
        Intent data = getActivity().getIntent();
        data.putExtra(FUEL_CATEGORY_KEY, fuelCategoryId);
        getTargetFragment().onActivityResult(
                getTargetRequestCode(), Activity.RESULT_OK, data
        );
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(FUEL_CATEGORY_KEY, fuelCategoryId);
        outState.putBoolean(FUEL_CATEGORY_CAN_CLEAR_CHOICE_KEY, canClearChoice);
        super.onSaveInstanceState(outState);
    }
}
