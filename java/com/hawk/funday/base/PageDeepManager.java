package com.hawk.funday.base;

import android.app.Fragment;

import com.hawk.funday.ui.fragment.detail.CommentListFragment;
import com.hawk.funday.ui.fragment.profile.ProfilePagerFragment;

import org.aisen.android.common.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Liyang Sun
 * @Description: 控制页面深度
 * @date 2016/9/20 11:26
 * @copyright HAWK
 */
public class PageDeepManager {

    private static final int MAX_NUMBER = 3;
    private static String[] classArr = new String[]{ CommentListFragment.class.getSimpleName(),
                                                     ProfilePagerFragment.class.getSimpleName() };
    private static final List<Fragment> fragments = new ArrayList<>();

    public static void addFragment(Fragment fragment) {
        try {
            if (!find(fragment)) {
                return;
            }

            fragments.add(fragment);

            for (int i = 0; i < classArr.length; i++) {
                checkFragmentDeep(classArr[i]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean find(Fragment fragment) {
        for (int i = 0; i < classArr.length; i++) {
            if (classArr[i].equals(fragment.getClass().getSimpleName())) {
                return true;
            }
        }

        return false;
    }

    private static void checkFragmentDeep(String className) {
        Fragment firstFragment = null;
        int count = 0;

        for (int i = 0; i < fragments.size(); i++) {
            if (fragments.get(i).getClass().getSimpleName().equals(className)) {
                count++;

                if (firstFragment == null) {
                    firstFragment = fragments.get(i);
                }

                if (count > MAX_NUMBER) {
                    if (fragments.remove(firstFragment)) {
                        if (firstFragment.getActivity() != null) {
                            firstFragment.getActivity().finish();
                        }
                    }

                    break;
                }
            }
        }
    }

    public static void removeFragment(Fragment fragment) {
        if (!find(fragment)) {
            return;
        }

        fragments.remove(fragment);
    }

}
