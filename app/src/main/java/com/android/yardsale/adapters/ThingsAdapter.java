package com.android.yardsale.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.yardsale.R;
import com.android.yardsale.activities.EditYardSaleActivity;
import com.android.yardsale.activities.ProgressDialog;
import com.android.yardsale.fragments.ListingsFragment;
import com.android.yardsale.fragments.MyFavoritesFragment;
import com.android.yardsale.helpers.YardSaleApplication;
import com.android.yardsale.helpers.image.CircleTransformation;
import com.android.yardsale.helpers.image.RoundedTransformation;
import com.android.yardsale.interfaces.OnAsyncTaskCompleted;
import com.android.yardsale.models.Item;
import com.android.yardsale.models.YardSale;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ThingsAdapter extends RecyclerView.Adapter<SaleViewHolder> implements OnAsyncTaskCompleted {

    public ParseQueryAdapter<YardSale> parseAdapter;
    private ViewGroup parseParent;
    public ThingsAdapter thingsAdapter = this;
    private YardSaleApplication client;
    private YardSale yardSale;
    private ImageView ivPic1;
    private ImageView ivPic2;
    private ImageView ivPic3;
    private ImageView ivPic4;
    private ImageButton btLike;
    private ImageView ivUserPic;
    private TextView tvSeller;
    //    private Button btShareSale;
//    private Button btDeleteSale;
//    private Button btEditSale;
    private Context myContext;
    //private TextView tvPostedAt;
    private TextView tvAddress;

    private List<YardSale> listSales;
    ImageView[] arrIv = {ivPic1, ivPic2, ivPic3, ivPic4};
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
    HashMap<SaleViewHolder, AnimatorSet> likeAnimations = new HashMap<>();
    FragmentManager fm;
    long then;

    public List<YardSale> getListSales() {
        return listSales;
    }

    public ThingsAdapter(FragmentManager fm, final Context context, ParseQueryAdapter.QueryFactory<YardSale> queryFactory, ViewGroup parentIn) {
        this.fm = fm;
        myContext = context;
        parseParent = parentIn;
        client = new YardSaleApplication();
        listSales = new ArrayList<>();
        parseAdapter = new ParseQueryAdapter<YardSale>(context, queryFactory) {

            @Override
            public View getItemView(YardSale sale, View v, ViewGroup parent) {
                if (v == null) {
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_yardsale, parent, false);
                }
                super.getItemView(sale, v, parent);

                myContext = context;
                yardSale = sale;
                listSales.add(sale);

                TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);

//                btDeleteSale = (Button) v.findViewById(R.id.btDeleteSale);
//                btEditSale = (Button) v.findViewById(R.id.btEditSale);
//                btShareSale = (Button) v.findViewById(R.id.btShareSale);
                btLike = (ImageButton) v.findViewById(R.id.btLike);
                ivPic1 = (ImageView) v.findViewById(R.id.ivPic1);
                ivPic2 = (ImageView) v.findViewById(R.id.ivPic2);
                ivPic3 = (ImageView) v.findViewById(R.id.ivPic3);
                ivPic4 = (ImageView) v.findViewById(R.id.ivPic4);
                ivUserPic = (ImageView) v.findViewById(R.id.ivUserPic);
                tvSeller = (TextView) v.findViewById(R.id.tvSeller);
                tvAddress = (TextView) v.findViewById(R.id.tvAddress);
                //tvPostedAt = (TextView) v.findViewById(R.id.tvPostedAt);
//                if(sale.getCreatedAt() !=null) {
//                    Date postedAtDate = sale.getCreatedAt();
//                    tvPostedAt.setText(DateUtils.getRelativeTimeSpanString(postedAtDate.getTime()));
//                }
                tvAddress.setText(sale.getAddress());
                if (sale.getSeller() != null) {
                    try {
                        tvSeller.setText(sale.getSeller().fetchIfNeeded().getUsername());

                        ParseUser seller = sale.getSeller();
                        if (seller.getParseFile("profile_pic") == null) {
                            if (seller.getString("profile_pic_url") == null) {
                                Picasso.with(getContext())
                                        .load(R.drawable.com_facebook_profile_picture_blank_square)
                                        .transform(new CircleTransformation())
                                        .into(ivUserPic);
                            } else {
                                Picasso.with(getContext())
                                        .load(seller.getString("profile_pic_url"))
                                        .transform(new RoundedTransformation(65, 0))
                                        .into(ivUserPic);
                            }
                        } else {
                            Picasso.with(getContext())
                                    .load(seller.getParseFile("profile_pic").getUrl())
                                    .transform(new CircleTransformation())
                                    .into(ivUserPic);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                tvTitle.setText(sale.getTitle());

//                    if (sale.getSeller() == ParseUser.getCurrentUser()) {
//                        btDeleteSale.setVisibility(View.VISIBLE);
//                        btEditSale.setVisibility(View.VISIBLE);
//                    } else {
//                        btDeleteSale.setVisibility(View.INVISIBLE);
//                        btEditSale.setVisibility(View.INVISIBLE);
//                    }

                Picasso.with(getContext()).load(R.drawable.placeholder).into(ivPic1);
                Picasso.with(getContext()).load(R.drawable.placeholder).into(ivPic2);
                Picasso.with(getContext()).load(R.drawable.placeholder).into(ivPic3);
                Picasso.with(getContext()).load(R.drawable.placeholder).into(ivPic4);
                int i = 0;
                ParseRelation<Item> rel = sale.getItemsRelation();
                try {
                    List<Item> itemList = rel.getQuery().find();
                    for (Item item : itemList) {
                        if (i == 4)
                            break;
                        if (item.getPhoto() != null) {
                            if (i == 0) {
                                Picasso.with(getContext()).load(item.getPhoto().getUrl()).placeholder(R.drawable.placeholder_loading).into(ivPic1);
                            }
                            if (i == 1) {
                                Picasso.with(getContext()).load(item.getPhoto().getUrl()).placeholder(R.drawable.placeholder_loading).into(ivPic2);
                            }
                            if (i == 2) {
                                Picasso.with(getContext()).load(item.getPhoto().getUrl()).placeholder(R.drawable.placeholder_loading).into(ivPic3);
                            }
                            if (i == 3) {
                                Picasso.with(getContext()).load(item.getPhoto().getUrl()).placeholder(R.drawable.placeholder_loading).into(ivPic4);
                            }
                            i++;
                        }
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return v;
            }
        };
        parseAdapter.addOnQueryLoadListener(new OnQueryLoadListener());
        parseAdapter.loadObjects();
    }

    @Override
    public SaleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_yardsale, parent, false);
        SaleViewHolder vh = new SaleViewHolder(v);
        return vh;
    }

    ThingsAdapter context;

    @Override
    public void onBindViewHolder(final SaleViewHolder holder, final int position) {
        parseAdapter.getView(position, holder.itemView, parseParent);

        //  client.setLikeForSale(fm, this, yardSale, btLike, false);
        //start progress diaog
        YardSale sale = yardSale;
        final ProgressDialog dialog = ProgressDialog.newInstance();
        dialog.show(fm, "");
        sale.getLikesRelation().getQuery().findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> results, ParseException e) {
                if (e == null) {
                    if (results.contains(client.getCurrentUser())) {
                        holder.btLike.setSelected(true);
                    } else {
                        holder.btLike.setSelected(false);
                    }
                    //stop dialog
                    dialog.dismiss();
                } else {
                    Log.d("ff", e.toString());
                    //stop dialog
                    dialog.dismiss();
                }
            }
        });

        context = this;
        btLike.setOnClickListener(new View.OnClickListener() {
            YardSale s = parseAdapter.getItem(position);

            @Override
            public void onClick(View v) {
                //Toast.makeText(myContext, "btn_like sale!", Toast.LENGTH_SHORT).show();

                client.setLikeForSale(fm, s, context);
//                //updateHeartButton(s, holder, true);
//                final ProgressDialog dialog = ProgressDialog.newInstance();
//                dialog.show(fm, "");
//                s.getLikesRelation().getQuery().findInBackground(new FindCallback<ParseUser>() {
//                    public void done(List<ParseUser> results, ParseException e) {
//                        if (e == null) {
//                            if (results.contains(client.getCurrentUser())) {
//                                s.addLikeForUser(client.getCurrentUser());
//                            } else {
//                                s.removeLikeForUser(client.getCurrentUser());
//                            }
//                            //stop dialog
//                            dialog.dismiss();
//                        } else {
//                            Log.d("ff", e.toString());
//                            //stop dialog
//                            dialog.dismiss();
//                        }
//                    }
//                });

            }
        });

//        btShareSale.setOnClickListener(new View.OnClickListener() {
//            YardSale s = parseAdapter.getItem(position);
//
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(myContext, "share sale!", Toast.LENGTH_SHORT).show();
//                client.shareSale(myContext, s);
//                parseAdapter.loadObjects();
//                thingsAdapter.notifyDataSetChanged();
//            }
//        });

//        ivCoverPic.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                YardSale s = parseAdapter.getItem(position);
//                client.getItemsForYardSale(myContext, s, ivCoverPic);
//            }
//        });

        for (int i = 0; i < arrIv.length; i++) {
            final int bkI = i;
            if (i == 0)
                arrIv[i] = ivPic1;
            if (i == 1)
                arrIv[i] = ivPic2;
            if (i == 2)
                arrIv[i] = ivPic3;
            if (i == 3)
                arrIv[i] = ivPic4;
            arrIv[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        then = (long) System.currentTimeMillis();
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if ((System.currentTimeMillis() - then) > 500) {
                /* Implement long click behavior here */
                            System.out.println("Long Click has happened!");
                            setPosition(holder.getPosition());
                            holder.showMenu();
                            return false;
                        } else {
                /* Implement short click behavior here or do nothing */
                            System.out.println("Short Click has happened...");
                            YardSale s = parseAdapter.getItem(position);
                            client.getItemsForYardSale(fm, myContext, s, arrIv[bkI]);
                            return true;
                        }
                    }
                    return true;
                }
            });
        }

//        btDeleteSale.setOnClickListener(new View.OnClickListener() {
//            YardSale s = parseAdapter.getItem(position);
//
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(myContext, "delete sale!", Toast.LENGTH_SHORT).show();
//                client.deleteSale(s);
//                thingsAdapter.parseAdapter.loadObjects();
//                thingsAdapter.notifyDataSetChanged();
//            }
//        });
//
//        btEditSale.setOnClickListener(new View.OnClickListener() {
//            YardSale s = parseAdapter.getItem(position);
//
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(myContext, "edit sale!", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(myContext, EditYardSaleActivity.class);
//                intent.putExtra("edit_yard_sale_id", s.getObjectId());
//                editYardSale(s);
//                ((Activity) myContext).startActivityForResult(intent, 20);
//                parseAdapter.loadObjects();
//                thingsAdapter.notifyDataSetChanged();
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return parseAdapter.getCount();
    }

    @Override
    public void onTaskCompleted() {
        parseAdapter.loadObjects();
        thingsAdapter.notifyDataSetChanged();
    }

    public class OnQueryLoadListener implements ParseQueryAdapter.OnQueryLoadListener<YardSale> {

        public void onLoading() {

        }

        public void onLoaded(List<YardSale> objects, Exception e) {

            if (parseAdapter.getCount() == 0) {
                Toast.makeText(myContext, "No items to display", Toast.LENGTH_SHORT).show();
                //TODO may be over ride this to show a message no wishlist present
                //http://stackoverflow.com/questions/27414173/equivalent-of-listview-setemptyview-in-recyclerview
            }

            thingsAdapter.notifyDataSetChanged();
        }
    }

    //these add and edit are to refresh the recyclerview
    public void editYardSale(YardSale row) {
        for (int i = 0; i < parseAdapter.getCount(); i++) {
            YardSale s = listSales.get(i);
            if (s.getObjectId().equals(row.getObjectId())) {
                s.setTitle(row.getTitle());
                s.setDescription(row.getDescription());
                s.setAddress(row.getAddress());
                if (row.getCoverPic() != null)
                    s.setCoverPic(row.getCoverPic());
                //sale.setLocation(row.getLocation());
                s.setStartTime(row.getStartTime());
                s.setEndTime(row.getEndTime());
                parseAdapter.loadObjects();
                thingsAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    public void addYardSale(YardSale addedYardSale) {
        boolean added = false;
        if (parseAdapter.getCount() > 0) {
            added = true;
            parseAdapter.notifyDataSetChanged();
        }
        if (!added)
            parseAdapter.notifyDataSetChanged();

        parseAdapter.notifyDataSetChanged();
    }

    public void removeYardSale(YardSale row) {
        parseAdapter.notifyDataSetChanged();
    }

    //these delete and edit methods are to fire the intent to parse for a sale
    public void fireEdit(Activity newContext, int position) {
        YardSale s = listSales.get(position);
        Toast.makeText(myContext, "edit sale!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(myContext, EditYardSaleActivity.class);
        intent.putExtra("edit_yard_sale_id", s.getObjectId());
        //editYardSale(s);
        ((Activity) myContext).startActivityForResult(intent, 20);
//        parseAdapter.loadObjects();
//        thingsAdapter.notifyDataSetChanged();
    }

    public void fireDelete(Activity newContext, int position, Fragment listener) {
        YardSale s = listSales.get(position);
        Toast.makeText(newContext, "delete sale!", Toast.LENGTH_SHORT).show();
        listSales.remove(position);
        if (listener instanceof MyFavoritesFragment)
            deleteSale(fm, s, (MyFavoritesFragment) listener);
        else if (listener instanceof ListingsFragment)
            deleteSale(fm, s, (ListingsFragment) listener);
//        parseAdapter.loadObjects();
//        thingsAdapter.notifyDataSetChanged();

    }

    public void fireShare(FragmentManager fm, Activity newContext, int position) {
        YardSale s = listSales.get(position);
        Toast.makeText(newContext, "share sale!", Toast.LENGTH_SHORT).show();
        client.shareSale(fm, myContext, s);
//        parseAdapter.loadObjects();
//        thingsAdapter.notifyDataSetChanged();
    }

    public void deleteSale(FragmentManager fm, YardSale sale, OnAsyncTaskCompleted listener) {
        this.fm = fm;
        //this.listener = listener;
        new DeleteSale().execute(sale);

//        Naturally we can also delete in an offline manner with:
//
//        todoItem.deleteEventually();
    }

    private class DeleteSale extends AsyncTask<YardSale, Void, String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            //show dialog
            dialog = ProgressDialog.newInstance();
            dialog.show(fm, "");
        }

        @Override
        protected String doInBackground(YardSale... params) {
            final YardSale sale = params[0];
            sale.deleteInBackground();

            ParseQuery<Item> query = ParseQuery.getQuery(Item.class);
            query.whereEqualTo("yardsale_id", sale);
            query.findInBackground(new FindCallback<Item>() {
                public void done(List<Item> itemList, ParseException e) {
                    if (e == null) {
                        for (Item item : itemList)
                            item.deleteInBackground();
                    } else {
                        Log.d("item", "Error: " + e.getMessage());
                    }
                }
            });
            //listener.onTaskCompleted();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //remove dialog
            dialog.dismiss();
            parseAdapter.loadObjects();
            thingsAdapter.notifyDataSetChanged();
        }
    }

    private void updateHeartButton(final YardSale sale, final SaleViewHolder holder, boolean animated) {
        if (animated) {
            if (!likeAnimations.containsKey(holder)) {
                AnimatorSet animatorSet = new AnimatorSet();
                likeAnimations.put(holder, animatorSet);

                ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(holder.btLike, "rotation", 0f, 360f);
                rotationAnim.setDuration(300);
                rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

//                ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(holder.btLike, "scaleX", 0.2f, 1f);
//                bounceAnimX.setDuration(300);
//                bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);
//
//                ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(holder.btLike, "scaleY", 0.2f, 1f);
//                bounceAnimY.setDuration(300);
//                bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
//                rotationAnim.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//                        if(sale.addLikeForUser();)
//                        holder.btLike.setImageResource(R.drawable.heartfilled);
//                    }
//                });
                animatorSet.play(rotationAnim);
                //   animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //resetLikeAnimationState(holder);
                        likeAnimations.remove(holder);
                        //holder.vBgLike.setVisibility(View.GONE);
                        //holder.ivLike.setVisibility(View.GONE);
                    }
                });

                animatorSet.start();
            }
        } else {
            if (likeAnimations.containsKey(holder.getPosition())) {
                holder.btLike.setImageResource(R.drawable.heartfilled);
            } else {
                holder.btLike.setImageResource(R.drawable.heartoutline);
            }
        }
    }

    private int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }


}
