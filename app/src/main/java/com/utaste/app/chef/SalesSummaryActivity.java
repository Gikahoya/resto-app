package com.utaste.app.chef;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.utaste.R;
import com.utaste.data.sqlite.SaleDao;
import com.utaste.domain.sale.RecipeSalesSummary;

import java.util.ArrayList;
import java.util.List;

public class SalesSummaryActivity extends AppCompatActivity {

    private RecyclerView rvSalesSummary;
    private TextView txtEmptySales;
    private RecipeSalesSummaryAdapter adapter;
    private final List<RecipeSalesSummary> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_summary);

        rvSalesSummary = findViewById(R.id.rvSalesSummary);
        txtEmptySales  = findViewById(R.id.txtEmptySales);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.bringToFront();
        btnBack.setOnClickListener(v -> {
            finish();
        });

        rvSalesSummary.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeSalesSummaryAdapter(data);
        rvSalesSummary.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SaleDao saleDao = new SaleDao(this);
        List<RecipeSalesSummary> fromDb = saleDao.getRecipeSalesSummary();

        data.clear();
        data.addAll(fromDb);
        adapter.notifyDataSetChanged();

        txtEmptySales.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
