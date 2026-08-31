package com.blankrk.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.blankrk.app.databinding.ActivityMainBinding
import com.blankrk.app.ui.chat.ChatListFragment
import com.blankrk.app.ui.following.FollowingFragment
import com.blankrk.app.ui.profile.ProfileFragment
import com.blankrk.app.ui.reels.ReelsFragment
import com.blankrk.app.ui.search.SearchFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(ReelsFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> ReelsFragment()
                R.id.nav_search -> SearchFragment()
                R.id.nav_following -> FollowingFragment()
                R.id.nav_chats -> ChatListFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> ReelsFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
