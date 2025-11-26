package com.utaste.app.chef;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.utaste.R;
import com.utaste.domain.sale.RecipeSalesSummary;

import java.util.List;

public class RecipeSalesSummaryAdapter extends RecyclerView.Adapter<RecipeSalesSummaryAdapter.VH> {

    private final List<RecipeSalesSummary> items;

    public RecipeSalesSummaryAdapter(List<RecipeSalesSummary> items) {
        this.items = items;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtRecipeName, txtSalesCount, txtAvgRating;

        VH(@NonNull View itemView) {
            super(itemView);
            txtRecipeName = itemView.findViewById(R.id.txtRecipeName);
            txtSalesCount = itemView.findViewById(R.id.txtSalesCount);
            txtAvgRating  = itemView.findViewById(R.id.txtAvgRating);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_sales_summary, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        RecipeSalesSummary summary = items.get(position);

        holder.txtRecipeName.setText(summary.getRecipeName());
        holder.txtSalesCount.setText("Sales: " + summary.getSalesCount());

        double avg = summary.getAverageRating();
        holder.txtAvgRating.setText(String.format("Rating: %.1f / 5", avg));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
