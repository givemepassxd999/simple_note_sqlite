package com.gg.givemepass.simplenotesqlite;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private List<String> datas;
    private Button addData;
    private EditText inputData;
    private NoteDBHelper helper;
    private Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initView() {
        addData = (Button) findViewById(R.id.add_data);
        inputData = (EditText) findViewById(R.id.input_edit_text);
        adapter = new MyAdapter();
        adapter.setData(datas);
        recyclerView = (RecyclerView) findViewById(R.id.list_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        addData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = inputData.getText().toString();
                if (inputText != null && !inputText.equals("")){
                    helper.add(inputText);
                    datas.add(inputText);
                    adapter.setData(datas);
                    adapter.notifyDataSetChanged();
                    inputData.setText("");
                } else{
                    Toast.makeText(MainActivity.this, "請輸入資料", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initData() {
        helper = new NoteDBHelper(MainActivity.this);
        cursor = helper.select();
        datas = new ArrayList<>();
        int count = cursor.getCount();
        for(int i = 0; i < count; i++){
            cursor.moveToPosition(i);
            String data = cursor.getString(1);
            if(data != null){
                datas.add(data);
            }
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<String> mData;
        private final String EDIT = "編輯";
        private final String DELETE = "刪除";
        private String[] items = {EDIT, DELETE};
        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.info_text);
            }
        }

        public MyAdapter() {
            mData = new ArrayList<>();
        }

        public void setData(List<String> data) {
            mData = data;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.mTextView.setText(mData.get(position));
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (items[which]){
                                        case EDIT:
                                            editRow(position);
                                            break;
                                        case DELETE:
                                            deleteRow(position);
                                            break;
                                    }
                                }
                            })
                            .show();
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        private void deleteRow(final int pos){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("刪除列")
                    .setMessage("你確定要刪除？")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cursor.moveToPosition(pos);
                            datas.remove(pos);
                            helper.delete(cursor.getInt(0));
                            notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }

        private void editRow(final int pos){
            final View item = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_layout, null);
            final EditText editText = (EditText) item.findViewById(R.id.edit_text);
            editText.setText(mData.get(pos));
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("編輯")
                    .setView(item)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String editInfo = editText.getText().toString();
                            if(editInfo != null && !editText.equals("")) {
                                cursor.moveToPosition(pos);
                                datas.set(pos, editInfo);
                                helper.update(cursor.getInt(0), editInfo);
                                notifyItemChanged(pos);
                            } else{
                                Toast.makeText(MainActivity.this, "請輸入文字", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.close();
    }
}
