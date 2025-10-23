package hi.petter.presentation.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import hi.petter.R
import hi.petter.databinding.ActivityContactListBinding

class ContactListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        // TODO: 实现初始化逻辑
        // 临时注释掉空状态显示，因为布局文件中没有这些View
        // binding.tvEmpty.visibility = android.view.View.VISIBLE
        // binding.rvContacts.visibility = android.view.View.GONE

        // TODO: 实现添加联系人功能 - 暂时注释掉，因为布局文件中没有fabAddContact
        // binding.fabAddContact.setOnClickListener {
        //     Toast.makeText(this, "添加联系人功能待实现", Toast.LENGTH_SHORT).show()
        // }

        // 设置ViewPager
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): androidx.fragment.app.Fragment {
                return when (position) {
                    0 -> ChatListFragment()
                    1 -> ContactListFragment()
                    else -> throw IllegalArgumentException("Invalid position")
                }
            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                binding.viewPager.currentItem = tab?.position ?: 0
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                // 可以在这里处理标签取消选择事件
            }

            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                // 可以在这里处理标签重新选择事件
            }
        })

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chats -> {
                    binding.viewPager.currentItem = 0
                    binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
                    true
                }
                R.id.nav_contacts -> {
                    binding.viewPager.currentItem = 1
                    binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1))
                    true
                }
                R.id.nav_discover -> {
                    // 打开消息测试界面
                    val intent = android.content.Intent(this, hi.petter.presentation.ui.test.MessageTestActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "个人中心功能待开发", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_contact_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // TODO: 实现搜索功能
                Toast.makeText(this, "搜索功能待开发", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_add_contact -> {
                // TODO: 实现添加联系人功能
                Toast.makeText(this, "添加联系人功能待开发", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        // 清除用户信息
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()

        // 简单地关闭Activity
        finish()
    }
}