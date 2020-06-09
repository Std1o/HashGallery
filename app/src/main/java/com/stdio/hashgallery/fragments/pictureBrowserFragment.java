package com.stdio.hashgallery.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stdio.hashgallery.R;
import com.stdio.hashgallery.utils.imageIndicatorListener;
import com.stdio.hashgallery.utils.pictureFacer;
import com.stdio.hashgallery.utils.recyclerViewPagerImageIndicator;

import java.util.ArrayList;

import static androidx.core.view.ViewCompat.setTransitionName;


/**
 * Author: CodeBoy722
 *
 * this fragment handles the browsing of all images in an ArrayList of pictureFacer passed in the constructor
 * the images are loaded in a ViewPager an a RecyclerView is used as a pager indicator for
 * each image in the ViewPager
 */
public class pictureBrowserFragment extends Fragment implements imageIndicatorListener {

    private  ArrayList<pictureFacer> allImages = new ArrayList<>();
    private int position;
    private Context animeContx;
    private ImageView image;
    private ViewPager imagePager;
    private int viewVisibilityController;
    private int viewVisibilitylooper;
    private ImagesPagerAdapter pagingImages;
    private int previousSelected = -1;
    private FloatingActionButton addBtn;

    public pictureBrowserFragment(){

    }

    public pictureBrowserFragment(ArrayList<pictureFacer> allImages, int imagePosition, Context anim) {
        this.allImages = allImages;
        this.position = imagePosition;
        this.animeContx = anim;
    }

    public static pictureBrowserFragment newInstance(ArrayList<pictureFacer> allImages, int imagePosition, Context anim) {
        pictureBrowserFragment fragment = new pictureBrowserFragment(allImages,imagePosition,anim);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.picture_browser, container, false);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**
         * initialisation of the recyclerView visibility control integers
         */
        viewVisibilityController = 0;
        viewVisibilitylooper = 0;

        /**
         * setting up the viewPager with images
         */
        imagePager = view.findViewById(R.id.imagePager);
        pagingImages = new ImagesPagerAdapter();
        imagePager.setAdapter(pagingImages);
        imagePager.setOffscreenPageLimit(3);
        imagePager.setCurrentItem(position);//displaying the image at the current position passed by the ImageDisplay Activity

        addBtn = view.findViewById(R.id.fab);
        //adjusting the recyclerView indicator to the current position of the viewPager, also highlights the image in recyclerView with respect to the
        //viewPager's position
        allImages.get(position).setSelected(true);
        previousSelected = position;


        /**
         * this listener controls the visibility of the recyclerView
         * indication and it current position in respect to the image ViewPager
         */
        imagePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if(previousSelected != -1){
                    allImages.get(previousSelected).setSelected(false);
                    previousSelected = position;
                    allImages.get(position).setSelected(true);
                }else{
                    previousSelected = position;
                    allImages.get(position).setSelected(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    /**
     * this method of the imageIndicatorListerner interface helps in communication between the fragment and the recyclerView Adapter
     * each time an iten in the adapter is clicked the position of that item is communicated in the fragment and the position of the
     * viewPager is adjusted as follows
     * @param ImagePosition The position of an image item in the RecyclerView Adapter
     */
    @Override
    public void onImageIndicatorClicked(int ImagePosition) {

        //the below lines of code highlights the currently select image in  the indicatorRecycler with respect to the viewPager position
        if(previousSelected != -1){
            allImages.get(previousSelected).setSelected(false);
            previousSelected = ImagePosition;
        }else{
            previousSelected = ImagePosition;
        }

        imagePager.setCurrentItem(ImagePosition);
    }

    /**
     * the imageViewPager's adapter
     */
    private class ImagesPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return allImages.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup containerCollection, int position) {
            LayoutInflater layoutinflater = (LayoutInflater) containerCollection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutinflater.inflate(R.layout.picture_browser_pager,null);
               image = view.findViewById(R.id.image);

            setTransitionName(image, String.valueOf(position)+"picture");

            pictureFacer pic = allImages.get(position);
            Glide.with(animeContx)
                    .load(pic.getPicturePath())
                    .apply(new RequestOptions().fitCenter())
                    .into(image);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(addBtn.isShown()){
                        addBtn.hide();
                    }else{
                        addBtn.show();
                    }

                    /**
                     * uncomment the below condition and comment the one above to control recyclerView visibility automatically
                     * when image is clicked
                     */
                    /*if(viewVisibilityController == 0){
                     indicatorRecycler.setVisibility(View.VISIBLE);
                     visibiling();
                 }else{
                     viewVisibilitylooper++;
                 }*/

                }
            });



            ((ViewPager) containerCollection).addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup containerCollection, int position, Object view) {
            ((ViewPager) containerCollection).removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == ((View) object);
        }
    }
}
