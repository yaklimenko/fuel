package ru.yaklimenko.fuel.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.List;

import ru.yaklimenko.fuel.FuelStationsMapActivity;
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

    /**
     * currently used on maps fuel type id
     */
    private Integer fuelCategoryId;

    /**
     * currently chosen item
     */
    private FuelCategory fuelCategory;

    public static FilterFuelDialogFragment getInstance(Integer fuelCategoryId) {
        FilterFuelDialogFragment f = new FilterFuelDialogFragment();
        if (fuelCategoryId != null) {
            Bundle args = new Bundle();
            args.putInt(FUEL_CATEGORY_KEY, fuelCategoryId);
            f.setArguments(args);
        }
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(FUEL_CATEGORY_KEY)) {
            fuelCategoryId = getArguments().getInt(FUEL_CATEGORY_KEY);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final List<FuelCategory> categories = FuelCategoryDao.getInstance().queryForAll();
        final FuelStationsMapActivity fuelStationsMapActivity =
                (FuelStationsMapActivity)getActivity();

        DialogInterface.OnClickListener selectedItemListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fuelCategory = categories.get(which);
            }
        };

        DialogInterface.OnClickListener clearClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fuelStationsMapActivity.onFuelFiltered(null);
            }
        };

        DialogInterface.OnClickListener okClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fuelStationsMapActivity.onFuelFiltered(fuelCategory);
            }
        };
        CharSequence[] catNames = new CharSequence[categories.size()];
        int checkedItemPosition = -1;
        for (int i = 0; i < categories.size(); i++) {
            FuelCategory category = categories.get(i);
            if (fuelCategoryId != null && fuelCategoryId == category.id) {
                checkedItemPosition = i;
            }
            catNames[i] = category.name;
        }
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_fuelfilter_title)
                .setSingleChoiceItems(catNames, checkedItemPosition, selectedItemListener)
                .setPositiveButton(R.string.dialog_ok, okClickListener)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setNeutralButton(R.string.dialog_fuelfilter_clear_button, clearClickListener)
                .create();
    }

}
