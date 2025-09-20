package com.example.ooxx.tools;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.ooxx.R;

import java.util.List;

public class RankingAdapter extends ArrayAdapter<RankingItem> {
    public RankingAdapter(@NonNull Context context, @NonNull List<RankingItem> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_rank, parent, false);
        }
        LinearLayout itemContainer = convertView.findViewById(R.id.item_container);
        TextView tvRank = convertView.findViewById(R.id.tv_rank);
        TextView tvName = convertView.findViewById(R.id.tv_name);
        TextView tvLevel = convertView.findViewById(R.id.tv_level);
        RankingItem item = getItem(position);
        tvRank.setText(String.valueOf(item.rank));
        tvName.setText(item.name);
        tvLevel.setText(item.level + " 分");
        int rankCircleColor;
        int itemBgColor;
        switch (item.rank) {
            case 1:
                rankCircleColor = ContextCompat.getColor(getContext(), R.color.gold);
                itemBgColor = ContextCompat.getColor(getContext(), R.color.gold_light);
                break;
            case 2:
                rankCircleColor = ContextCompat.getColor(getContext(), R.color.silver);
                itemBgColor = ContextCompat.getColor(getContext(), R.color.silver_light);
                break;
            case 3:
                rankCircleColor = ContextCompat.getColor(getContext(), R.color.bronze);
                itemBgColor = ContextCompat.getColor(getContext(), R.color.bronze_light);
                break;
            default:
                rankCircleColor = ContextCompat.getColor(getContext(), R.color.light_gray);
                itemBgColor = ContextCompat.getColor(getContext(), R.color.white);
                break;
        }
        GradientDrawable rankBg = new GradientDrawable();
        rankBg.setShape(GradientDrawable.OVAL);
        rankBg.setColor(rankCircleColor);
        tvRank.setBackground(rankBg);
        GradientDrawable itemBg = new GradientDrawable();
        itemBg.setShape(GradientDrawable.RECTANGLE);
        itemBg.setCornerRadius(dpToPx(8));
        itemBg.setColor(itemBgColor);
        if (item.rank > 3) {
            itemBg.setStroke(dpToPx(1), ContextCompat.getColor(getContext(), R.color.light_gray));
        }
        itemContainer.setBackground(itemBg);
        return convertView;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density);
    }
}
