package com.nvite.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nvite.R;
import com.nvite.adapter.ContactListAdapter;

public class ContactlistActivity extends BaseActivity {
    RecyclerView recycle_view;
    ContactListAdapter adapter;
    private MenuItem menu_search, menu_more;
    private TextView add_new_group;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactlist);
        recycle_view = (RecyclerView) findViewById(R.id.recycle_view);
        add_new_group = (TextView) findViewById(R.id.add_new_group);
        back = (ImageView) findViewById(R.id.back);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        DividerItemDecoration verticalDecoration = new DividerItemDecoration(recycle_view.getContext(), DividerItemDecoration.VERTICAL);
        Drawable verticalDivider = ContextCompat.getDrawable(ContactlistActivity.this, R.drawable.recycle_divider);
        verticalDecoration.setDrawable(verticalDivider);
        recycle_view.addItemDecoration(verticalDecoration);
        LinearLayoutManager linearLayoutManagerBooks = new LinearLayoutManager(ContactlistActivity.this, LinearLayoutManager.VERTICAL, false);
        recycle_view.setLayoutManager(linearLayoutManagerBooks);

        adapter = new ContactListAdapter(ContactlistActivity.this);
        recycle_view.setAdapter(adapter);


        add_new_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_new_group.setVisibility(View.GONE);
                Intent intent = new Intent(ContactlistActivity.this, NewGroupActivity.class);
                startActivity(intent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.popup, menu);
        menu_search = menu.findItem(R.id.menu_search);
        menu_more = menu.findItem(R.id.menu_more);
        if(getIntent().getBooleanExtra("from_event_detail",false)){
            menu_more.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_more:
                if (add_new_group.getVisibility() == View.VISIBLE) {
                    add_new_group.setVisibility(View.GONE);
                } else {
                    add_new_group.setVisibility(View.VISIBLE);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}




