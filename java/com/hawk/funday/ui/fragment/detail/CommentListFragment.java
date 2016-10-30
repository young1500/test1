package com.hawk.funday.ui.fragment.detail;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hawk.funday.R;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.base.Consts;
import com.hawk.funday.base.PageDeepManager;
import com.hawk.funday.support.db.FundayDB;
import com.hawk.funday.support.eventbus.CommentEvent;
import com.hawk.funday.support.eventbus.ICommentSubscriber;
import com.hawk.funday.support.eventbus.IPostFavoriteSubscriber;
import com.hawk.funday.support.eventbus.PostDestoryEvent;
import com.hawk.funday.support.eventbus.PostFavoriteEvent;
import com.hawk.funday.support.paging.OffsetPaging;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.AccountBean;
import com.hawk.funday.support.sdk.bean.BaseBean;
import com.hawk.funday.support.sdk.bean.CommentBean;
import com.hawk.funday.support.sdk.bean.CommentsBean;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.utils.FundayExceptionDelegate;
import com.hawk.funday.support.utils.FundayUtils;
import com.hawk.funday.ui.activity.base.ContainerActivity;
import com.hawk.funday.ui.fragment.base.BizFragment;
import com.tma.analytics.TmaAgent;
import com.wcc.framework.notification.NotificationCenter;

import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.android.ui.fragment.ARecycleViewSwipeRefreshFragment;
import org.aisen.android.ui.fragment.itemview.AHeaderItemViewCreator;
import org.aisen.android.ui.fragment.itemview.DefDividerItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.android.ui.widget.AsToolbar;

import java.util.List;

/**
 * @Description: CommentListFragment 评论列表分页
 * @author  qiangtai.huang
 * @date  2016/8/22
 * @copyright TCL-HAWK
 */
public class CommentListFragment extends ARecycleViewSwipeRefreshFragment<CommentBean, CommentsBean, PostBean>
                                    implements View.OnClickListener, AsToolbar.OnToolbarDoubleClick {

    public static void launch(Activity from, PostBean bean, boolean sticky) {

        launch(from, bean, sticky,0);
    }
    public static void launch(Activity from, PostBean bean, boolean sticky,int favDrawable) {
        if (bean==null || bean.getUser() == null)
            return;

        FragmentArgs args = new FragmentArgs();
        args.add("bean", bean);
        args.add("sticky", String.valueOf(sticky));
        args.add("favDrawable",favDrawable);
        ContainerActivity.launch(from, CommentListFragment.class, args);
    }
    private int mFavDrawable;////不同从怎么分页进入时设置收藏成功颜色
    private PostBean mPost;// 被评论的内容
    @ViewInject(id = R.id.btn_send)
    private View mSendBtn; ///发布评论按钮
    @ViewInject(id = R.id.edt_comment)
    private EditText mCommentEdt; //// 评论编辑框
    @ViewInject(id = R.id.layRecycle)
    FrameLayout layRecycle;

    private boolean sticky;// 如果是从CmtBtn点击跳转过来的，自动滑动至StickyView
    private View viewHeader;
    private ViewGroup laySticky;
    private View viewSticky;
    private CommentHeaderView commentHeaderView;
    @Override
    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);
        DefDividerItemView divider = new DefDividerItemView(getActivity(), getResources().getColor(R.color.divider_timeline_item));
        divider.setSize(1.5f);
        getRefreshView().addItemDecoration(divider);
        getRefreshView().setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        activity.getSupportActionBar().setTitle(R.string.title_cmts);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_content_detail, menu);
        // create report menu
        SubMenu subMenu = menu.addSubMenu(R.id.comments, R.id.toolbar_report_content, 1, R.string.report_post);
        String[] items = getResources().getStringArray(R.array.report_content_type);
        for (int i = 0; i < items.length; i++) {
            subMenu.add(100, i, i, items[i]);
        }
        setMenuItemVisible(menu, AppContext.getLoginedAccount());
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()){
            // repost comment
            case 0:
            case 1:
                BizFragment.createBizFragment(getActivity()).checkUserPermission(new BizFragment.OnUserPermissionCallback() {

                    @Override
                    public void onSuccess(AccountBean account) {
                        if (mPost != null && account != null
                                && mPost.getUser() != null && getActivity() != null
                                && mPost.getUser().getId() == account.getUserId()) {
                            getActivity().invalidateOptionsMenu();
                        }
                        else {
                            reportComment(item.getItemId());
                        }
                    }

                    @Override
                    public void onFaild() {

                    }

                });
                break;
            case R.id.toolbar_cancel_content:
                if (getActivity()!=null) {
                    new MaterialDialog.Builder(getActivity())
                            .content(R.string.cancel_post_tip)
                            .positiveText(R.string.btn_ok)
                            .negativeText(R.string.btn_cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    deletePost();
                                }
                            })
                            .show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setMenuItemVisible(Menu menu, AccountBean account){
        if (mPost!=null&&account != null
                &&  mPost.getUser()!=null
                && mPost.getUser().getId() ==account.getUserId()) {
            menu.findItem(R.id.toolbar_cancel_content).setVisible(true);
            menu.findItem(R.id.toolbar_report_content).setVisible(false);
        }else {
            menu.findItem(R.id.toolbar_cancel_content).setVisible(false);
            menu.findItem(R.id.toolbar_report_content).setVisible(true);
        }
    }

    private void reportComment(final int index){
        final String[] reportTypeArr = new String[]{ "RI", "PV" };

        if (index >= reportTypeArr.length) {
            return;
        }
        Bundle args = new Bundle();
        args.putString("type", reportTypeArr[index]);
        TmaAgent.onEvent(getActivity(), Consts.Event.Event_post_report, args);
        new WorkTask<Void,Void,BaseBean>(){

            @Override
            protected void onPrepare() {
                super.onPrepare();

                ViewUtils.createProgressDialog(getActivity(),getResources().getString(R.string.reporting_post), FundayUtils.getThemeColor(getActivity())).show();
            }

            @Override
            public BaseBean workInBackground(Void... voids) throws TaskException {
                return FundaySDK.newInstance().doReportResource( mPost.getResourceId() , mPost.getResourceType() , reportTypeArr[index]);
            }
            @Override
            protected void onFailure(TaskException exception) {
                super.onFailure(exception);

                showMessage(exception.getMessage());
            }

            @Override
            protected void onFinished() {
                super.onFinished();

                ViewUtils.dismissProgressDialog();
            }

            @Override
            protected void onSuccess(BaseBean s) {
                super.onSuccess(s);

                showMessage(R.string.report_post_success);
            }

        }.execute();
    }

    private void deletePost(){
        TmaAgent.onEvent(getActivity(), Consts.Event.Event_post_destory);

        new WorkTask<Void,Void,BaseBean>(){

            @Override
            protected void onPrepare() {
                super.onPrepare();

                ViewUtils.createProgressDialog(getActivity(), getString(R.string.deleting_post), FundayUtils.getThemeColor(getActivity())).show();
            }

            @Override
            public BaseBean workInBackground(Void... voids) throws TaskException {
                final AccountBean userBean = AppContext.getLoginedAccount();

                BaseBean baseBean = FundaySDK.newInstance().doCancelPost(mPost.getResourceId() , mPost.getResourceType() , userBean.getToken(), userBean.getOpenId());

                // 同步删除DB的数据
                String whereClause = " resourceId = ? ";
                String[] whereArgs = new String[]{ mPost.getResourceId() + "" };
                try {
                    FundayDB.getCacheDB().delete(PostBean.class, whereClause, whereArgs);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                NotificationCenter.defaultCenter().publish(new PostDestoryEvent(mPost));

                return baseBean;
            }

            @Override
            protected void onFailure(TaskException exception) {
                super.onFailure(exception);

                showMessage(FundayExceptionDelegate.getMessage(getActivity(), exception, R.string.delete_post_failure));
            }

            @Override
            protected void onSuccess(BaseBean s) {
                super.onSuccess(s);

                showMessage(R.string.delete_post_success);

                if (getActivity() != null){
                   getActivity().finish();
                }
            }

            @Override
            protected void onFinished() {
                super.onFinished();

                ViewUtils.dismissProgressDialog();
            }

        }.execute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPost = savedInstanceState == null ? (PostBean) getArguments().getSerializable("bean")
                                           : (PostBean) savedInstanceState.getSerializable("bean");
        sticky = savedInstanceState == null ? Boolean.parseBoolean(getArguments().getString("sticky"))
                                            : savedInstanceState.getBoolean("sticky");
        mFavDrawable = savedInstanceState == null ? getArguments().getInt("favDrawable", 0)
                                                  : savedInstanceState.getInt("favDrawable", 0);
        setHasOptionsMenu(true);

        NotificationCenter.defaultCenter().subscriber(PostFavoriteEvent.class, postFavoriteSubscriber);
        NotificationCenter.defaultCenter().subscriber(CommentEvent.class, postCmtSubscriber);

        PageDeepManager.addFragment(CommentListFragment.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        NotificationCenter.defaultCenter().unsubscribe(PostFavoriteEvent.class, postFavoriteSubscriber);
        NotificationCenter.defaultCenter().unsubscribe(CommentEvent.class, postCmtSubscriber);

        PageDeepManager.removeFragment(CommentListFragment.this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", mPost);
        outState.putBoolean("sticky", sticky);
        outState.putInt("favDrawable", mFavDrawable);
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        attchEvent();
    }

    @Override
    protected IItemViewCreator<CommentBean> configFooterViewCreator() {
        return com.hawk.funday.support.utils.ViewUtils.configFooterViewCreator(getActivity(), this);
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_fg_comment_list;
    }

    @Override
    protected void setupRefreshViewWithConfig(RefreshConfig config) {
        super.setupRefreshViewWithConfig(config);

        getRefreshView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (viewSticky != null) {
                    synchronized (viewSticky) {
                        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        int position = layoutManager.findFirstVisibleItemPosition();
                        if (position == 0) {
                            View view = layoutManager.findViewByPosition(0);
                            if (view.getBottom() <= laySticky.getHeight()) {
                                if (viewSticky.getParent() == laySticky) {
                                    laySticky.removeView(viewSticky);
                                    layRecycle.addView(viewSticky);
                                }
                            }
                            else {
                                if (viewSticky.getParent() == layRecycle) {
                                    layRecycle.removeView(viewSticky);
                                    laySticky.addView(viewSticky);
                                }
                            }
                        }
                        else if (position > 0) {
                            if (viewSticky.getHeight() > 0 && layRecycle.getHeight() > 0) {
                                if (viewSticky.getParent() == laySticky) {
                                    laySticky.removeView(viewSticky);
                                    layRecycle.addView(viewSticky);
                                }
                            }
                        }
                    }
                }
            }

        });
    }

    @Override
    protected IPaging<CommentBean, CommentsBean> newPaging() {
        return new OffsetPaging<>();
    }

    @Override
    public void requestData(RefreshMode refreshMode) {
        new CmtTask(refreshMode != RefreshMode.update ? RefreshMode.reset : RefreshMode.update).execute();
    }

    @Override
    protected AHeaderItemViewCreator<PostBean> configHeaderViewCreator() {
        return new AHeaderItemViewCreator<PostBean>() {
            @Override
            public int[][] setHeaders() {
                return new int[][]{ { R.layout.item_cmts_header, 100 } };
            }
            @Override
            public IITemView<PostBean> newItemView(View view, int viewType) {
                commentHeaderView = new CommentHeaderView(getActivity(), view, CommentListFragment.this);
                commentHeaderView.setmFavDrawable(mFavDrawable);
                commentHeaderView.onBindView(view);
                commentHeaderView.onBindData(view, mPost, 0);
                viewHeader = view;
                laySticky = (ViewGroup) view.findViewById(R.id.laySticky);
                viewSticky = laySticky.getChildAt(0);
                return commentHeaderView;
            }
        };
    }

    private void attchEvent(){
        mSendBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.btn_send:
            sendComment();
            break;
        }
    }
    /**
      * @Description: 提交评论
      * @return  
      */
    
    private synchronized void sendComment(){
        hindSoftInput();
        final String comment=mCommentEdt.getText().toString().trim();
        if (comment.length()<1 ||getActivity()==null)
            return;
        BizFragment.createBizFragment(getActivity()).checkUserPermission(new BizFragment.OnUserPermissionCallback() {
            @Override
            public void onSuccess(AccountBean account) {
                new WorkTask<Void,Void,CommentBean>(){

                    @Override
                    protected void onPrepare() {
                        super.onPrepare();
                        if (getActivity()!=null &&getResources()!=null)
                         ViewUtils.createProgressDialog(getActivity(), getResources().getString(R.string.posting_comment), FundayUtils.getThemeColor(getActivity())).show();
                    }

                    @Override
                    public CommentBean workInBackground(Void... voids) throws TaskException {
                        long reId=mPost.getResourceId();
                        int reType=mPost.getResourceType();
                        String content=comment;
                        String url=mPost.getThumbnailUrls()[0].getUrl();
                        CommentBean bean= FundaySDK.newInstance().doSendComment(reId,reType,content,url,-1);

                        mPost.setCommentCount(mPost.getCommentCount() + 1);

                        ContentValues values = new ContentValues();
                        values.put("commentCount", mPost.getCommentCount());
                        String whereClause = " resourceId = ? ";
                        String[] whereArgs = new String[]{ mPost.getResourceId() + "" };
                        try {
                            FundayDB.getCacheDB().update(PostBean.class, values, whereClause, whereArgs);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return bean;
                    }

                    @Override
                    protected void onFailure(TaskException exception) {
                        super.onFailure(exception);

                        if (getActivity() != null) {
                            TmaAgent.onEvent(getActivity(), Consts.Event.Event_comment_create_faild);
                        }

                        showMessage(FundayExceptionDelegate.getMessage(getActivity(), exception, R.string.posted_comment_failure));
                    }

                    @Override
                    protected void onSuccess(CommentBean commentBean) {
                        super.onSuccess(commentBean);

                        NotificationCenter.defaultCenter().publish(new CommentEvent(CommentEvent.Type.create, mPost, commentBean));

                        if (getActivity()!=null) {
                            showMessage(R.string.posted_comment_success);

                            TmaAgent.onEvent(getActivity(), Consts.Event.Event_comment_create_success);

                            /////新增的评论添加到列表第一位
                            onToolbarDoubleClick();

                            mCommentEdt.setText("");
                        }
                    }

                    @Override
                    protected void onFinished() {
                        super.onFinished();

                        ViewUtils.dismissProgressDialog();
                    }

                }.execute();
            }

            @Override
            public void onFaild() {

            }
        });
    }

    /**
     * 删除评论
     *
     * @param comment
     */
    void destoryComment(CommentBean comment) {
        BizFragment bizFragment = BizFragment.createBizFragment(this);
        if (bizFragment != null) {
            bizFragment.destoryComment(mPost, comment);
        }
    }

    @Override
    public IItemViewCreator<CommentBean> configItemViewCreator() {
        return new IItemViewCreator<CommentBean>() {
            @Override
            public View newContentView(LayoutInflater layoutInflater, ViewGroup viewGroup, int i) {
                return layoutInflater.inflate(R.layout.item_comment,viewGroup,false);
            }
            @Override
            public IITemView<CommentBean> newItemView(View view, int i) {
                return new CommentItemView(getActivity(),view, CommentListFragment.this);
            }
        };
    }

    private void hindSoftInput(){
        if (  getActivity()!=null &&getView()!=null) {
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    IPostFavoriteSubscriber postFavoriteSubscriber = new IPostFavoriteSubscriber() {

        @Override
        public void onEvent(PostFavoriteEvent event) {
            if (event.getTag().getResourceId() == mPost.getResourceId() && commentHeaderView != null) {
                commentHeaderView.setFavBtnBackground(event.isFav());

                mPost.setFavorite(event.isFav());
            }
        }

    };

    ICommentSubscriber postCmtSubscriber = new ICommentSubscriber() {

        @Override
        public void onEvent(CommentEvent event) {
            // 删除或者新增
            if (mPost.getResourceId() == event.getPost().getResourceId()) {
                // 删除
                if (event.getType() == CommentEvent.Type.destory) {
                    // 列表已经加载的数据
                    if (FundayUtils.removeCommentStatus(getAdapterItems(), event.getComment())) {
                        getAdapter().notifyDataSetChanged();
                    }
                }

                mPost.setCommentCount(event.getPost().getCommentCount());

                commentHeaderView.setCmtNum(mPost);
            }
        }

    };

    class CmtTask extends APagingTask<Void, Void, CommentsBean> {

        public CmtTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected List<CommentBean> parseResult(CommentsBean commentsBean) {
            return commentsBean.getComments();
        }

        @Override
        protected CommentsBean workInBackground(RefreshMode refreshMode, String s, String s1, Void... voids) throws TaskException {
            int offset = 0;

            if (!TextUtils.isEmpty(s1)) {
                offset = Integer.parseInt(s1);
            }
            CommentsBean bean = FundaySDK.newInstance().getComments(mPost.getResourceId(), mPost.getResourceType(), offset);
            bean.setEndPaging(bean.getOffset() == -1);
            return bean;
        }

        @Override
        protected void onSuccess(CommentsBean commentsBean) {
            super.onSuccess(commentsBean);

            if (sticky && commentsBean != null && commentsBean.getComments() != null && commentsBean.getComments().size() > 0) {
                if (getActivity() != null && getTaskCount(getTaskId()) == 1) {
                    smoothToSticky();
                }
            }
        }

    }

    private void smoothToSticky() {
        if (viewHeader.getHeight() > 0) {
            LinearLayoutManager manager = (LinearLayoutManager) getRefreshView().getLayoutManager();
            manager.scrollToPositionWithOffset(0, (viewHeader.getHeight() - viewSticky.getHeight() + getRefreshView().getPaddingTop()) * -1);
        }
        else {
            viewHeader.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    viewHeader.getViewTreeObserver().removeOnPreDrawListener(this);

                    LinearLayoutManager manager = (LinearLayoutManager) getRefreshView().getLayoutManager();
                    manager.scrollToPositionWithOffset(0, (viewHeader.getHeight() - viewSticky.getHeight() + getRefreshView().getPaddingTop()) * -1);

                    return true;
                }

            });
        }
    }

    @Override
    public boolean onToolbarDoubleClick() {
        smoothToSticky();
        requestDataDelaySetRefreshing(Consts.REQUEST_DATA_DELAY);

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        TmaAgent.onPageStart(Consts.Page.Page_comments);
        TmaAgent.onResume(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();

        TmaAgent.onPageEnd(Consts.Page.Page_comments);
        TmaAgent.onPause(getActivity());
    }

}
