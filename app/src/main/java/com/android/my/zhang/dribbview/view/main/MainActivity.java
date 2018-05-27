package com.android.my.zhang.dribbview.view.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.my.zhang.dribbview.R;
import com.android.my.zhang.dribbview.dribbble.auth.Dribbble;
import com.android.my.zhang.dribbview.view.bucket_list.BucketListFragment;
import com.android.my.zhang.dribbview.view.follow_list.FollowListFragment;
import com.android.my.zhang.dribbview.view.shot_list.ShotListFragment;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.drawer) NavigationView navigationView;

    private final String saveState = "main_save_state";
    private ActionBarDrawerToggle drawerToggle;
    private int preNavItemId = R.id.drawer_item_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setupDrawer();

        if (savedInstanceState != null) {
            /*
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, ShotListFragment.newInstance(ShotListFragment.LIST_TYPE_ALL))
                    .commit();
            */
            Fragment fragment = null;
            int preNavItemId = savedInstanceState.getInt(saveState);
            switch (preNavItemId) {

                case R.id.drawer_item_likes:
                    fragment = ShotListFragment.newInstance(ShotListFragment.LIST_TYPE_LIKE);
                    setTitle(R.string.title_likes);
                    //setTitle("Test");
                    break;
                case R.id.drawer_item_buckets:
                    fragment = BucketListFragment.newInstance(false, null);
                    setTitle(R.string.title_buckets);
                    break;
                case R.id.drawer_item_follows:
                    fragment = ShotListFragment.newInstance(ShotListFragment.LIST_TYPE_FOLLOW);
                    setTitle(R.string.title_follow);
                    //setTitle("Test");
                    break;
                case R.id.drawer_item_followed:
                    fragment = FollowListFragment.newInstance();
                    setTitle(R.string.title_following);
                    break;
                default:
                    fragment = ShotListFragment.newInstance(ShotListFragment.LIST_TYPE_ALL);
                    setTitle(R.string.title_home);
                    break;
            }
            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }

        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, ShotListFragment.newInstance(ShotListFragment.LIST_TYPE_ALL))
                    .commit();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.isChecked()) {
                Log.d("save state:", "checked: " + item.getItemId());
                outState.putInt(saveState, item.getItemId());
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawer() {
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,          /* DrawerLayout object */
                R.string.open_drawer,         /* "open drawer" description */
                R.string.close_drawer         /* "close drawer" description */
        );

        drawerLayout.setDrawerListener(drawerToggle);
        //drawerLayout.addDrawerListener(drawerToggle);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (item.isChecked()) {
                    drawerLayout.closeDrawers();
                    return true;
                }

                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.drawer_item_home:
                        fragment = ShotListFragment.newInstance(ShotListFragment.LIST_TYPE_ALL);
                        setTitle(R.string.title_home);
                        break;
                    case R.id.drawer_item_likes:
                        fragment = ShotListFragment.newInstance(ShotListFragment.LIST_TYPE_LIKE);
                        setTitle(R.string.title_likes);
                        //setTitle("Test");
                        break;
                    case R.id.drawer_item_follows:
                        fragment = ShotListFragment.newInstance(ShotListFragment.LIST_TYPE_FOLLOW);
                        setTitle(R.string.title_follow);
                        //setTitle("Test");
                        break;
                    case R.id.drawer_item_buckets:
                        fragment = BucketListFragment.newInstance(false, null);
                        setTitle(R.string.title_buckets);
                        break;

                    case R.id.drawer_item_followed:
                        fragment = FollowListFragment.newInstance();
                        setTitle(R.string.title_following);
                        break;

                }

                drawerLayout.closeDrawers();

                if (fragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                    return true;
                }

                return false;
            }
        });

        setupNavHeader();
    }

    private void setupNavHeader() {
        View headerView = navigationView.getHeaderView(0);

        ((TextView) headerView.findViewById(R.id.nav_header_user_name)).setText(
                Dribbble.getCurrentUser().name);

        ((SimpleDraweeView) headerView.findViewById(R.id.nav_header_user_picture))
                .setImageURI(Uri.parse(Dribbble.getCurrentUser().avatar_url));

        headerView.findViewById(R.id.nav_header_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dribbble.logout(MainActivity.this);

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
