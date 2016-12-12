package com.bakkenbaeck.sol.ui;

import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.databinding.FragmentInfoBinding;
import com.bakkenbaeck.sol.util.CurrentPhase;
import com.bakkenbaeck.sol.util.SolPreferences;

public class InfoFragment extends Fragment {
    public static final String TAG = "InfoFragment";
    public static final String PHASE_NAME = "InfoFragment";

    private boolean notifications;
    private String phaseName;

    public static InfoFragment newInstance(final String phaseName) {
        Log.d(TAG, "newInstance: " + phaseName);
        Bundle b = new Bundle();
        b.putString(PHASE_NAME, phaseName);
        InfoFragment f = new InfoFragment();
        f.setArguments(b);
        return f;
    }

    private FragmentInfoBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_info, null, false);

        init();

        return binding.getRoot();
    }

    private void init() {
        if (this.phaseName == null) {
            this.phaseName = getArguments().getString(PHASE_NAME);
        }

        setColors(phaseName);

        this.binding.titleWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((SunActivity) getActivity()).onBackPressed();
            }
        });

        SolPreferences solPrefs = new SolPreferences(getContext());
        notifications = solPrefs.getShowNotification();

        String s = notifications
                ? "off"
                : "on";

        this.binding.notificationValue.setText(s);

        this.binding.notificationWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifications = !notifications;

                String s = notifications
                        ? "off"
                        : "on";

                binding.notificationValue.setText(s);
                SolPreferences solPrefs = new SolPreferences(getContext());
                solPrefs.cacheShowNotification(notifications);
            }
        });
    }

    private void setColors(final String phaseName) {
        CurrentPhase phase = new CurrentPhase(phaseName);
        final String infoMessage = this.getContext().getResources().getString(R.string.info_message);
        final String formatedInfo = infoMessage.replace("{color}", String.valueOf(ContextCompat
                .getColor(this.getContext(), phase.getPrimaryColor())));
        Spanned s = convertToHtml(formatedInfo);
        this.binding.infoMessage.setText(s);

        this.binding.title.setTextColor(ContextCompat.getColor(this.getContext(), phase.getSecondaryColor()));
        this.binding.sunCircle.setColorFilter(ContextCompat.getColor(this.getContext(), phase.getSecondaryColor()), PorterDuff.Mode.SRC);
        this.binding.root.setBackgroundColor(ContextCompat.getColor(this.getContext(), phase.getBackgroundColor()));
        this.binding.infoMessage.setTextColor(ContextCompat.getColor(this.getContext(), phase.getSecondaryColor()));
        this.binding.notificationText.setTextColor(ContextCompat.getColor(this.getContext(), phase.getSecondaryColor()));
        this.binding.notificationValue.setTextColor(ContextCompat.getColor(this.getContext(), phase.getPrimaryColor()));
    }

    public void update(final String phaseName) {
        if (this.binding == null) {
            return;
        }

        this.phaseName = phaseName;
        setColors(phaseName);
    }

    private Spanned convertToHtml(final String message) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(message);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        phaseName = null;

        if (binding != null) {
            binding.unbind();
            binding = null;
        }
    }
}
