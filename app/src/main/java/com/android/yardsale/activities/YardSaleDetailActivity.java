package com.android.yardsale.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.yardsale.R;
import com.android.yardsale.adapters.HeaderRecyclerViewAdapterV2;
import com.android.yardsale.adapters.ItemsAdapter;
import com.android.yardsale.fragments.SaleMapFragment;
import com.android.yardsale.helpers.MyView;
import com.android.yardsale.helpers.YardSaleApplication;
import com.android.yardsale.helpers.image.CircleTransformation;
import com.android.yardsale.helpers.image.RoundedTransformation;
import com.android.yardsale.models.Item;
import com.android.yardsale.models.YardSale;
import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class YardSaleDetailActivity extends FragmentActivity {
    private MyView rvItems;
    private ItemsAdapter aItems;
    HeaderRecyclerViewAdapterV2 hAdapter;
    private List<Item> itemList;
    private FloatingActionButton btCreateItem;
    private YardSale sale;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvDateTime;
    private TextView tvAddress;
    private TextView tvSeller;
    private ImageView ivUserPic;
    private TextView tvCompleteTime;
    LinearLayoutManager llm;
    Activity thisactivity;
    YardSaleApplication client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO back button
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yard_sale_detail);

        rvItems = (MyView) findViewById(R.id.rvItems);
        btCreateItem = (FloatingActionButton) findViewById(R.id.fab);
        btCreateItem.attachToRecyclerView(rvItems);
        btCreateItem.setColorNormal(getResources().getColor(R.color.accent_color));

        itemList = new ArrayList<>();
        client = new YardSaleApplication();

        final List<CharSequence> itemsObjList = getIntent().getCharSequenceArrayListExtra("item_list");
        final String yardsaleObj = getIntent().getStringExtra("yardsale");

        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvItems.setHasFixedSize(true);
        rvItems.setLayoutManager(llm);
        rvItems.setItemAnimator(new DefaultItemAnimator());

        aItems = new ItemsAdapter(getSupportFragmentManager(), getBaseContext(), itemList);
        hAdapter = new HeaderRecyclerViewAdapterV2(aItems);

        rvItems.setAdapter(hAdapter);
        registerForContextMenu(rvItems);
        thisactivity = this;

        if (itemsObjList != null) {
            for (CharSequence objId : itemsObjList) {
                ParseQuery<Item> query = ParseQuery.getQuery(Item.class);
                //query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK); // or CACHE_ONLY
                query.getInBackground((String) objId, new GetCallback<Item>() {
                    public void done(Item item, ParseException e) {
                        if (e == null) {
                            itemList.add(item);
                            hAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }
        if (yardsaleObj != null) {
            ParseQuery<YardSale> query = ParseQuery.getQuery(YardSale.class);
            //query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK); // or CACHE_ONLY
            query.getInBackground(yardsaleObj, new GetCallback<YardSale>() {
                public void done(final YardSale sale, ParseException e) {
                    if (e == null) {
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View head = inflater.inflate(R.layout.item_list_header, null);

                        tvTitle = (TextView) head.findViewById(R.id.tvTitle);
                        tvDescription = (TextView) head.findViewById(R.id.tvDescription);
                        tvAddress = (TextView) head.findViewById(R.id.tvAddress);
                        tvDateTime = (TextView) head.findViewById(R.id.tvDateTime);
                        tvSeller = (TextView) head.findViewById(R.id.tvSeller);
                        ivUserPic = (ImageView) head.findViewById(R.id.ivUserPic);
                        tvCompleteTime = (TextView) head.findViewById(R.id.tvCompleteTime);
                        //cvHeader = (CardView) findViewById(R.id.cvHeader);

                        tvTitle.setText(sale.getTitle());
                        tvDescription.setTextColor(Color.WHITE);
                        tvTitle.setTextColor(Color.WHITE);
                        tvDescription.setText(sale.getDescription());
                        Date startDate = sale.getStartTime();
                        tvDateTime.setTextColor(Color.WHITE);
                        tvDateTime.setText(DateUtils.getRelativeTimeSpanString(startDate.getTime()));
                        tvCompleteTime.setTextColor(Color.WHITE);
                        tvCompleteTime.setText("Start " + startDate.toString() + "\nEnd " + sale.getEndTime().toString());
                        tvAddress.setTextColor(Color.WHITE);
                        tvAddress.setText(sale.getAddress());
                        if (sale.getSeller() != null) {
                            try {
                                tvSeller.setTextColor(Color.WHITE);
                                tvSeller.setText("Hosted by " + sale.getSeller().fetchIfNeeded().getUsername());
                                ParseUser seller = sale.getSeller();
                                if (seller.getParseFile("profile_pic") == null) {
                                    if (seller.getString("profile_pic_url") == null) {
                                        Picasso.with(getBaseContext())
                                                .load(R.drawable.com_facebook_profile_picture_blank_square)
                                                .transform(new CircleTransformation())
                                                .into(ivUserPic);
                                    } else {
                                        Picasso.with(getBaseContext())
                                                .load(seller.getString("profile_pic_url"))
                                                .transform(new RoundedTransformation(65, 0))
                                                .into(ivUserPic);
                                    }
                                } else {
                                    Picasso.with(getBaseContext())
                                            .load(seller.getParseFile("profile_pic").getUrl())
                                            .transform(new CircleTransformation())
                                            .into(ivUserPic);
                                }
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                        }

                        hAdapter.addHeaderView(head);

                        View footer = inflater.inflate(R.layout.item_list_footer, null);
                        Fragment map = getSupportFragmentManager().findFragmentById(R.id.fragMap);
                        ((SaleMapFragment) map).addMarker(sale);

                        hAdapter.addFooterView(footer);
                        setSale(sale);
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        ParseUser owner = sale.getSeller();
                        if (currentUser.getObjectId().equals(owner.getObjectId())) {
                            btCreateItem.setVisibility(View.VISIBLE);
                        } else {
                            btCreateItem.setVisibility(View.INVISIBLE);
                        }

                        btCreateItem.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(getBaseContext(), "creating item!!!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(YardSaleDetailActivity.this, AddItemActivity.class);
                                intent.putExtra("yard_sale_id", sale.getObjectId());
                                startActivityForResult(intent, 22);
                            }
                        });

                    }
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 143) {
            Toast.makeText(getBaseContext(), "yardsaledetailedactivity", Toast.LENGTH_SHORT).show();
            String price = data.getStringExtra("price");
            String desc = data.getStringExtra("desc");
            final String objId = data.getStringExtra("obj_id");
            if (objId == null) {
                ParseQuery<Item> query = Item.getQuery();
                //query.whereEqualTo("price", price);
                query.whereEqualTo("description", desc);
                //query.whereEqualTo("seller", YardSaleApplication.getCurrentUser());

                query.findInBackground(new FindCallback<Item>() {
                    @Override
                    public void done(List<Item> items, ParseException e) {
                        if (e == null) {
                            Item item = items.get(0);
                            itemList.add(item);
                            aItems.notifyDataSetChanged();

                            // itemlist add
                            YardSale s = item.getYardSale();
                            s.getItemsRelation().add(item);
                            s.saveInBackground();
                        }
                    }
                });

            } else {
                ParseQuery<Item> query = ParseQuery.getQuery(Item.class);
                query.getInBackground(objId, new GetCallback<Item>() {
                    public void done(Item item, ParseException e) {
                        if (e == null) {
                            for (Item i : itemList) {
                                if (i.getObjectId().equals(objId)) {
                                    //if photo changed then update itemlist
                                    YardSale s = i.getYardSale();
                                    if (i.getPhoto() != item.getPhoto()) {
                                        s.getItemsRelation().remove(i);
                                        s.getItemsRelation().add(item);
                                        s.saveInBackground();
                                    }
                                    i.setDescription(item.getDescription());
                                    ParseFile old = i.getPhoto();
                                    i.setPhoto(item.getPhoto());
                                    i.setPrice(item.getPrice());

                                    aItems.notifyDataSetChanged();
                                }
                            }
                        } else {
                            // something went wrong
                        }
                    }
                });
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_yard_sale_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if(id == R.id.ic_info){
//            Intent intent = new Intent(this,YardSaleInfoActivity.class);
//            intent.putExtra("yardsale_id", sale.getObjectId());
//            startActivity(intent);
//        }
        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setSale(YardSale sale) {
        this.sale = sale;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        MyView.RecyclerContextMenuInfo info = (MyView.RecyclerContextMenuInfo) item.getMenuInfo();
        int position = -1;
        try {
            int numHeaders = hAdapter.getHeaderCount();
            position = aItems.getPosition() - numHeaders;

        } catch (Exception e) {
            Log.d("Error", e.getLocalizedMessage(), e);
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.delete_item:
                Toast.makeText(this, "delete item!", Toast.LENGTH_SHORT).show();
                aItems.delete(this, position);
                break;
            case R.id.edit_item:
                Toast.makeText(this, "edit item!", Toast.LENGTH_SHORT).show();
                aItems.edit(this, position);
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //TODO fix the theme color for this one
        // inflate menu here
        menu.setHeaderTitle("Select action:");
        menu.add(Menu.NONE, R.id.edit_item, Menu.NONE, "EDIT");
        menu.add(Menu.NONE, R.id.delete_item, Menu.NONE, "DELETE");
    }


}