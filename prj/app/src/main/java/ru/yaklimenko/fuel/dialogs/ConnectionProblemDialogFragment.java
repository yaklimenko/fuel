package ru.yaklimenko.fuel.dialogs;

import android.app.*;
import android.os.Bundle;

import ru.yaklimenko.fuel.R;

/**
 * Created by Антон on 31.05.2016.
 * dialog for connection problems
 */
public class ConnectionProblemDialogFragment extends DialogFragment {

    public static final String MESSAGE_RES = "messageRes";
    public static final String TAG = ConnectionProblemDialogFragment.class.getSimpleName();

    private int messageRes;

    public static ConnectionProblemDialogFragment getInstance(int messageRes) {
        ConnectionProblemDialogFragment f = new ConnectionProblemDialogFragment();
        Bundle args = new Bundle();
        args.putInt(MESSAGE_RES, messageRes);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messageRes = getArguments().getInt(MESSAGE_RES);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_title_error)
                .setMessage(messageRes)
                .setPositiveButton(R.string.dialog_ok, null)
                .create();
    }
}
