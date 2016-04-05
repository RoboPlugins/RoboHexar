package com.nordstrom.app.checkout.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nordstrom.analytics.AnalyticsConstants;
import com.nordstrom.app.BuildConfig;
import com.nordstrom.app.R;
import com.nordstrom.app.account.activity.ActivityRequestCodes;
import com.nordstrom.app.account.component.SubtotalComponent;
import com.nordstrom.app.address.fragment.CreateContactAddressActivity;
import com.nordstrom.app.address.presentation.AddressPresentationHelper;
import com.nordstrom.app.bag.analytics.CheckoutTransaction;
import com.nordstrom.app.bag.presentation.BagAndCartPresentationHelper;
import com.nordstrom.app.checkout.analytics.CheckoutAnalyticsDispatch;
import com.nordstrom.app.checkout.component.AbstractPaymentCardItemView;
import com.nordstrom.app.checkout.component.AddressComponentItem;
import com.nordstrom.app.checkout.component.CheckoutRewardsSectionComponent;
import com.nordstrom.app.checkout.component.OrderItemGridView;
import com.nordstrom.app.checkout.component.ReviewStoreItemsComponent;
import com.nordstrom.app.checkout.component.ShippingMethodComponentItem;
import com.nordstrom.app.checkout.controller.LoyaltyCheckoutController;
import com.nordstrom.app.checkout.model.ItemsPerDeliveryMethod;
import com.nordstrom.app.checkout.observable.PlaceOrderHandler;
import com.nordstrom.app.checkout.presentation.OrderSummaryPresentationHelper;
import com.nordstrom.app.main.application.AppConstants;
import com.nordstrom.app.main.application.AppPreferences;
import com.nordstrom.app.main.application.NordstromApplication;
import com.nordstrom.app.main.event.CartChangedEvent;
import com.nordstrom.app.main.fragment.NordstromBaseFragment;
import com.nordstrom.app.main.lifecycle.ActivityLifecycleObservables;
import com.nordstrom.app.main.lifecycle.ActivityResult;
import com.nordstrom.app.main.service.NordstromSessionService;
import com.nordstrom.app.main.session.RdsfSession;
import com.nordstrom.app.main.session.UserSession;
import com.nordstrom.app.main.util.BusProvider;
import com.nordstrom.app.main.util.JsonBundler;
import com.nordstrom.app.main.util.Log;
import com.nordstrom.app.main.util.NordstromScheduler;
import com.nordstrom.app.main.util.RdsfPresentationHelper;
import com.nordstrom.app.main.util.StringUtils;
import com.nordstrom.app.main.widget.NordstromNestedScrollView;
import com.nordstrom.app.paymentmethods.activity.AddNewCreditCardActivity;
import com.nordstrom.app.paymentmethods.presentation.PaymentMethodPresentationHelper;
import com.nordstrom.app.rewards.activity.LoyaltyEarnEnrollActivity;
import com.nordstrom.domain.rewards.LoyaltyDomain;
import com.nordstrom.domain.rewards.presentation.CheckoutRewardsSectionPresentation;
import com.nordstrom.services.account.dao.AccountDAO;
import com.nordstrom.services.addresses.model.Address;
import com.nordstrom.services.addresses.model.AddressBook;
import com.nordstrom.services.base.model.SessionState;
import com.nordstrom.services.base.util.ErrorResponseUtil;
import com.nordstrom.services.checkout.dao.CheckoutDAO;
import com.nordstrom.services.checkout.model.AbstractItem;
import com.nordstrom.services.checkout.model.CVV;
import com.nordstrom.services.checkout.model.OrderSummary;
import com.nordstrom.services.checkout.model.ShippingMethod;
import com.nordstrom.services.checkout.model.submitorder.SubmitOrderResponse;
import com.nordstrom.services.paymentmethods.model.CreditCard;
import com.nordstrom.services.paymentmethods.model.PaymentMethods;
import com.nordstrom.services.rewards.model.LoyaltyStatus;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;

public class CheckoutFragment extends NordstromBaseFragment {

    //region INJECTED CLASSES ----------------------------------------------------------------------

    @Inject
    AccountDAO mAccountDAO;

    @Inject
    ActivityLifecycleObservables mActivityLifecycleObservables;

    @Inject
    AddNewCreditCardActivity.IntentFactory mAddNewCreditCardActivityIntentFactory;

    @Inject
    AddressPresentationHelper mAddressPresentationHelper;

    @Inject
    AppPreferences mAppPreferences;

    @Inject
    CheckoutAnalyticsDispatch mCheckoutAnalyticsDispatch;

    @Inject
    CheckoutDAO mCheckoutDAO;

    @Inject
    CheckoutTransaction mCheckoutTransaction;

    @Inject
    LoyaltyCheckoutController mLoyaltyCheckoutController;

    @Inject
    LoyaltyDomain mLoyaltyDomain;

    @Inject
    LoyaltyEarnEnrollActivity.IntentFactory mLoyaltyEarnEnrollActivityIntentFactory;

    @Inject
    NordstromSessionService mNordstromSessionService;

    @Inject
    OrderSummaryPresentationHelper mOrderSummaryPresentationHelper;

    @Inject
    PaymentMethodPresentationHelper mPaymentMethodPresentationHelper;

    @Inject
    PlaceOrderHandler mPlaceOrderHandler;

    @Inject
    RdsfPresentationHelper mRdsfPresentationHelper;

    @Inject
    RdsfSession mRdsfSession;

    @Inject
    NordstromScheduler mScheduler;

    @Inject
    UserSession mUserSession;

    @Inject
    SubtotalComponent.PresentationFactory mSubtotalComponentPresentationFactory;

    //endregion

    //region INJECTED VIEWS ------------------------------------------------------------------------

    @Bind(R.id.checkout_add_shipping_address_tv)
    View mAddShippingAddressTextView;

    @Bind(R.id.checkout_select_shipping_method_tv)
    View mSelectShippingMethodTv;

    @Bind(R.id.checkout_shipping_not_eligible_tv)
    View mShippingNotEligible;

    @Bind(R.id.checkout_shipping_method_caret)
    ImageView mShippingMethodCaret;

    @Bind(R.id.checkout_fragment_address)
    View mCheckOutAddress;

    @Bind(R.id.checkout_fragment_shipping_methods_layout)
    RelativeLayout mShippingMethodsLayout;

    @Bind(R.id.checkout_fragment_shipping_method_selected)
    ShippingMethodComponentItem mShippingMethodComponentItem;

    @Bind(R.id.checkout_fragment_payment_card)
    AbstractPaymentCardItemView mCheckoutPaymentCard;

    @Bind(R.id.checkout_fragment_add_credit_debit)
    TextView mCheckoutAddCreditDebitLabel;

    @Bind(R.id.checkout_fragment_payment_gift)
    View mCheckoutPaymentGift;

    @Bind(R.id.checkout_fragment_promo_display_text)
    TextView mCheckoutPromoDisplayText;

    @Bind(R.id.checkout_fragment_place_order_button)
    Button mCheckoutPlaceOrderButton;

    @Bind(R.id.checkout_fragment_shipping_address_item)
    AddressComponentItem mAddressComponentItem;

    @Bind(R.id.checkout_fragment_shipping_items_title)
    TextView mShippingItemsTitle;

    @Bind(R.id.checkout_fragment_product_images_layout)
    OrderItemGridView mProductImages;

    @Bind(R.id.checkout_fragment_product_images_clickable_area)
    View mProductImageClickableArea;

    @Bind(R.id.checkout_fragment_cvv_field)
    MaterialEditText mCvvEditText;

    @Bind(R.id.checkout_fragment_scroll_view)
    NordstromNestedScrollView mScrollView;

    @Bind(R.id.checkout_shipping_to_address_layout)
    View mShipToAddressSection;

    @Bind(R.id.checkout_fragment_pickup_methods_container)
    ViewGroup mPickupMethodContainer;

    // Email items section
    @Bind(R.id.checkout_email_to_address_layout)
    View mEmailLayout;

    @Bind(R.id.checkout_fragment_email_product_images_layout)
    OrderItemGridView mEmailProductImages;

    @Bind(R.id.checkout_fragment_email_product_images_clickable_area)
    View mEmailProductImageClickableArea;

    @Bind(R.id.checkout_fragment_email_items_title)
    TextView mEmailItemsLayoutTitle;

    @Bind(R.id.checkout_fragment_rewards_section_component)
    CheckoutRewardsSectionComponent mCheckoutRewardsSectionComponent;

    @Bind(R.id.checkout_fragment_subtotal_component)
    SubtotalComponent mSubtotalComponent;

    //endregion

    //region LOCAL STATICS -------------------------------------------------------------------------
    private static final String TAG = CheckoutFragment.class.getSimpleName();

    //endregion

    //region CLASS VARIABLES -----------------------------------------------------------------------

    private OrderSummary mOrderSummary;
    private String mOrderSummaryError;
    private boolean mApiDataFetchedByOnActivityResult = false;

    //endregion

    //region CONSTRUCTOR ---------------------------------------------------------------------------

    /**
     * Creates a new instance of an {@link CheckoutFragment} with the given parameters and
     * calls {@link #setArguments} on the fragment before returning. This is the preferred method to
     * create a new fragment with parameters in Android because Android requires Fragments to have
     * a parameterless constructor and will re-use the arguments Bundle.
     *
     * @return a new instance of the {@link CheckoutFragment}.
     */
    public static CheckoutFragment newInstance() {
        // If parameters are added to this method, then create a Bundle and call setArguments()
        // on the fragment before returning it.
        CheckoutFragment fragment = new CheckoutFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    //endregion

    //region LIFECYCLE METHODS ---------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NordstromApplication.getInjector().getActivityComponent(getActivity()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.checkout_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getView() != null) {
            getView().setAlpha(0f);
        }

        mAddressComponentItem.setCheckIconVisible(false);

        mCheckOutAddress.setOnClickListener(new AddressClickListener());
        mShippingMethodsLayout.setOnClickListener(new ShippingMethodClickListener());
        mCheckoutPaymentCard.setOnClickListener(new PaymentCardClickListener());
        mCheckoutPaymentGift.setOnClickListener(new PaymentGiftClickListener());
        mCheckoutPlaceOrderButton.setOnClickListener(new PlaceOrderClickListener());

        mShippingItemsTitle.setText(String.format(getString(R.string.checkout_shipping_item_single_title), "0"));

        String pageName = String.format(
                AnalyticsConstants.ANALYTICS_CHECKOUT_REVIEW_ORDER,
                isLoggedIn() ? AnalyticsConstants.ANALYTICS_REGISTERED : AnalyticsConstants.ANALYTICS_GUEST);

        mAnalytics.view(pageName, AnalyticsConstants.ANALYTICS_CATEGORY_ID_CHECKOUT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ActivityRequestCodes.CREATE_CONTACT_ADDRESS_ACTIVITY.ordinal() &&
                resultCode == Activity.RESULT_OK) {

            showLoadingIndicatorOpaque(R.string.checkout_placing_order_indicator_message);

            Observable<OrderSummary> orderSummaryObservable;

            if (mNordstromSessionService.isLoggedIn()) {
                orderSummaryObservable = mCheckoutDAO.getRegisteredOrderSummary(mNordstromSessionService.getSessionState());
            } else {
                orderSummaryObservable = mCheckoutDAO.getGuestOrderSummary(mNordstromSessionService.getSessionState());
            }

            //API Get the latest OrderSummary
            //Validate OrderSummary
            //If valid Place Order
            mCompositeSubscription.add(orderSummaryObservable
                    .observeOn(mScheduler.mainThread()) //Rx operators run on main thread.
                    .filter(orderSummary -> {

                        updateToOrderSummary(orderSummary);
                        updateUi();

                        Boolean validOrder = validateOrder();
                        if (!validOrder) {
                            hideLoadingIndicatorIgnoreCount();
                        }
                        return validOrder;
                    })
                    .flatMap(orderSummary -> getSubmitOrderObservable())
                    .subscribe(new SubmitOrderResponseObserver()));
        } else if (requestCode == ActivityRequestCodes.ADD_NEW_CREDIT_CARD_ACTIVITY.ordinal() &&
                resultCode == Activity.RESULT_OK) {

            // This case represents when a card is added directly by the AddNewCreditCardActivity because there were
            // no previous cards. That is, there was no intermediate presentation of the CheckoutPaymentMethodFragment.

            // Apply newly added card for registered users to order (guest user card will already be applied to order)
            if (mNordstromSessionService.isLoggedIn() && data != null &&
                    data.hasExtra(AddNewCreditCardActivity.EXTRA_ADDED_CREDIT_CARD)) {
                CreditCard addedCard = JsonBundler.get(data.getExtras(), AddNewCreditCardActivity.EXTRA_ADDED_CREDIT_CARD, CreditCard.class);
                apiSetRegisteredCreditCardId(addedCard);
            }
        } else if (requestCode == ActivityRequestCodes.LOYALTY_EARN_ENROLL_ACTIVITY.ordinal() && resultCode == Activity.RESULT_OK) {
            LoyaltyStatus status = mLoyaltyEarnEnrollActivityIntentFactory.getLoyaltySessionStateResult(data);
            mLoyaltyCheckoutController.userEnrolledWithStatus(status);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mCompositeSubscription.add(mActivityLifecycleObservables.getActivityResultObservable()
                .observeOn(mScheduler.mainThread())
                .subscribe(new ActivityResultObserver()));

        // The mApiDataFetchedByOnActivityResult flag is used to prevent a race condition between onResume and onActivityResult.
        // When opening the AddNewCreditCardActivity to create a new credit card, the api calls for this page need to
        // wait until after the new card is attached to the order.
        if (!mApiDataFetchedByOnActivityResult) {
            showLoadingIndicatorTransparent(getView());
            createCompositeApiCallObservable()
                    .observeOn(mScheduler.mainThread())
                    .subscribe(new CompositeOrderSummaryResultObserver());
        }
        mApiDataFetchedByOnActivityResult = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mCvvEditText.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    //endregion

    //region WIDGET --------------------------------------------------------------------------------

    @Override
    public Uri getCurrentUri() {
        return Uri.parse(AppConstants.Routes.PaymentMethods.PAYMENTMETHODS_CHECKOUT);
    }

    @Override
    public String getTitle() {
        return getString(R.string.title_checkout);
    }

    /*
     * Enable the Up button.
     */
    @Override
    public boolean isActionBarBackButton() {
        return true;
    }

    @Override
    public boolean isActiveLoggedInSessionRequired() {
        return true;
    }

    @Override
    public boolean onBackPressed() {
        return onUpPressed();
    }

    @Override
    public boolean onUpPressed() {
        if (!mNordstromSessionService.isLoggedIn()) {
            // Clear the shipping, billing and encrypted card
            mUserSession.clearGuestBillingAndShippingData();
        }

        // Go all the way back to the bag skipping any intermediary fragments
        mRouteChanger.goBackToPath(Uri.parse(AppConstants.Routes.Bag.BAG));
        return true;
    }

    /**
     * Fetches the CVV from either the user-inputted text field or automatically for certain Nordstrom tender cards. It
     * may even be null for certain other Nordstrom tender cards.
     *
     * @param creditCard current credit card for the purchase.
     * @return a cvv to send the api, possibly null.
     */
    @VisibleForTesting
    @Nullable
    CVV getCvv(CreditCard creditCard) {
        CVV cvv = new CVV();
        // If the card is a non-visa nordstrom card, the CVV is 000
        if (mRdsfPresentationHelper.isOld9DigitNordstromDebitOrRetailCard(creditCard)) {
            cvv.setCreditCardIdentifier(AppConstants.Checkout.OLD_NINE_DIGIT_NORDSTROM_RETAIL_CVV);
        } else if (mCvvEditText.getVisibility() == View.VISIBLE) {
            // Get the CVV from the edit text
            cvv.setCreditCardIdentifier(mCvvEditText.getText().toString());
        } else {
            // Return the (possibly null) Cvv in the UserSession
            cvv = mUserSession.getCreditCardCvv();
        }

        return cvv;
    }

    //endregion

    //region LISTENERS -----------------------------------------------------------------------------

    private class AddressClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mUserSession.hasValidShippingAddress() && !mNordstromSessionService.isLoggedIn()) {
                if (mUserSession.getShippingAddress() != null) {
                    mUserSession.setSelectedAddress(mUserSession.getShippingAddress().makeCopy());
                }
                mRouteChanger.routeChange(Uri.parse(AppConstants.Routes.Account.ACCOUNT_ADDRESS_BOOK_EDIT));
            } else {
                routeToShippingFragment();
            }
        }
    }

    /**
     * Listens for click events on the checkout rewards component and routes to the loyalty earn enroll fragment when
     * clicked.
     */
    @VisibleForTesting
    class CheckoutComponentClickListener implements CheckoutRewardsSectionComponent.CheckoutRewardsSectionComponentListener {
        @Override
        public void onCaretClicked() {
            Intent intent = mLoyaltyEarnEnrollActivityIntentFactory.createActivityStartIntent();
            mActivityRouter.startActivityForResult(CheckoutFragment.this, intent, ActivityRequestCodes.LOYALTY_EARN_ENROLL_ACTIVITY);
        }
    }

    private class DialogAddContactAddressClickListener implements View.OnClickListener {

        private final Dialog mDialog;

        public DialogAddContactAddressClickListener(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public void onClick(View v) {
            mDialog.dismiss();
            Intent intent = new Intent(getActivity(), CreateContactAddressActivity.class);
            mActivityRouter.startActivityForResult(CheckoutFragment.this, intent, ActivityRequestCodes.CREATE_CONTACT_ADDRESS_ACTIVITY);
        }
    }

    private class DialogUseShippingClickListener implements View.OnClickListener {

        private final Dialog mDialog;
        private final Address mContactAddress;

        public DialogUseShippingClickListener(Dialog dialog, Address address) {
            mDialog = dialog;
            mContactAddress = address;
        }

        @Override
        public void onClick(View view) {
            mDialog.dismiss();

            //set the shipping address as the contact address
            showLoadingIndicatorOpaque(R.string.checkout_placing_order_indicator_message);
            mCompositeSubscription.add(mCheckoutDAO.postRegisteredBillingAddress(mNordstromSessionService.getSessionState(), mContactAddress)
                    .flatMap(addressResponse -> getSubmitOrderObservable())
                    .observeOn(mScheduler.mainThread())
                    .subscribe(new SubmitOrderResponseObserver()));
        }
    }

    private class ShippingMethodClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            routeToShippingFragment();
        }
    }

    private class PaymentCardClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            CreditCard currentlySelectedCreditCard = mUserSession.getCurrentlySelectedCreditCard();
            boolean hasNoCurrentlySelectedCard = currentlySelectedCreditCard == null ||
                    currentlySelectedCreditCard.getId() == 0;
            boolean hasNoSavedCreditCards = mUserSession.getPaymentMethods() != null &&
                    mUserSession.getPaymentMethods().getCreditCards() != null &&
                    mUserSession.getPaymentMethods().getCreditCards().size() == 0;

            if (hasNoCurrentlySelectedCard && hasNoSavedCreditCards) {
                startAddCreditCardActivity();
                mCheckoutAnalyticsDispatch.viewCheckoutCardEntry();
            } else {
                mRouteChanger.routeChange(Uri.parse(AppConstants.Routes.CheckOut.CHECKOUT_PAYMENT));
            }
        }
    }

    private class PaymentGiftClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mRouteChanger.routeChange(Uri.parse(AppConstants.Routes.CheckOut.CHECKOUT_PAYMENT));
        }
    }

    private class PlaceOrderClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if (validateOrder()) {
                conditionallySubmitOrder();
            }
        }
    }

    private class ItemsPerDeliveryClickListener implements View.OnClickListener {
        private ItemsPerDeliveryMethod mItemsPerPickup;

        private ItemsPerDeliveryClickListener(ItemsPerDeliveryMethod itemsPerPickup) {
            mItemsPerPickup = itemsPerPickup;
        }

        @Override
        public void onClick(View view) {
            mUserSession.setOrderSummary(mOrderSummary);
            mRouteChanger.routeChange(CheckoutReviewItemsFragment.newInstance(mItemsPerPickup));
        }
    }

    //endregion

    //region EVENTS --------------------------------------------------------------------------------
    //endregion

    //region PRIVATE METHODS -----------------------------------------------------------------------

    @VisibleForTesting
    void conditionallySubmitOrder() {
        // subscribe to the observable which will determine if order checkout should proceed
        // and kick off the conditional dialog which will emit an item for the above observer
        mPlaceOrderHandler.getShouldPlaceOrder(getActivity())
                .observeOn(mScheduler.mainThread())
                .subscribeOn(mScheduler.mainThread())
                .subscribe(new ShouldSubmitOrderObservable());
    }

    @VisibleForTesting
    boolean validateOrder() {

        // Check bag errors
        if (!StringUtils.isNullOrEmpty(mOrderSummaryError)) {
            mNordstromErrorMessengerManager.showErrorMessengerWithText(mOrderSummaryError);
            return false;
        } else if (BagAndCartPresentationHelper.orderSummaryContainsUnavailableItems(mOrderSummary)) {
            mNordstromErrorMessengerManager.showErrorMessengerWithText(R.string.bag_item_unavailable_error_message);
            return false;
        }

        CreditCard currentlySelectedCreditCard = mUserSession.getCurrentlySelectedCreditCard();

        // if RDSF enabled and old card is deactivated, disallow it
        if (mAppPreferences.getFeatureEnabledRdsf() && mRdsfSession.isPhaseDeactivatedOrAfter() &&
                mRdsfPresentationHelper.isOld9DigitNordstromDebitOrRetailCard(currentlySelectedCreditCard)) {
            mNordstromErrorMessengerManager.showErrorMessengerWithText(R.string.checkout_error_bad_credit_card);
            return false;
        }

        // check CVV
        if (mCvvEditText.getVisibility() == View.VISIBLE) {
            String error = mPaymentMethodPresentationHelper.getValidationErrorForCvv(currentlySelectedCreditCard, mCvvEditText.getText().toString());
            mCvvEditText.setError(error);

            if (error != null) {
                mNordstromErrorMessengerManager.showErrorMessengerWithText(R.string.checkout_please_enter_cvv);
                mScrollView.smoothScrollTo(0, 0);
                mCvvEditText.requestFocus();
                return false;
            }
        }

        // check adequate funds
        if ((currentlySelectedCreditCard != null) &&
                (currentlySelectedCreditCard.getId() <= 0l
                        && mOrderSummary.getInvoice().getNetTotal() > 0d)) {
            // YOU HAVE NOT ENOUGH MINERALS or VESPENE GAS
            mNordstromErrorMessengerManager.showErrorMessengerWithText(R.string.checkout_error_remaining_balance);
            return false;
        }

        if (!Boolean.TRUE.equals(mOrderSummary.getHasShipAddress()) && BagAndCartPresentationHelper.doesCartContainShippableItems(mOrderSummary)) {
            mNordstromErrorMessengerManager.showErrorMessengerWithText(R.string.checkout_error_shipping_address_needed);
            return false;
        }

        // There are some situations where we first need to add a contact address. Check this and prompt here
        return !launchAdditionalContactAddressRequestIfNeeded();

    }

    private void startAddCreditCardActivity() {
        Intent intent = mAddNewCreditCardActivityIntentFactory.createIntentModeCheckout();
        mActivityRouter.startActivityForResult(this, intent, ActivityRequestCodes.ADD_NEW_CREDIT_CARD_ACTIVITY);
    }

    /**
     * Sets {@link OrderSummary} state.
     *
     * @param orderSummary order summary.
     */
    @VisibleForTesting
    void updateToOrderSummary(OrderSummary orderSummary) {
        mOrderSummary = orderSummary;

        mUserSession.setOrderSummary(orderSummary);

        mUserSession.setCurrentlySelectedCreditCard(orderSummary.getCreditCard());

        // Show error messages
        mOrderSummaryError = BagAndCartPresentationHelper.getOrderSummaryErrorMessages(orderSummary);
        if (!mOrderSummaryError.isEmpty()) {
            mNordstromErrorMessengerManager.showErrorMessengerWithText(mOrderSummaryError);
        }
    }

    /**
     * Creates composite api call observable.
     *
     * @return the composite response observable.
     */
    @VisibleForTesting
    Observable<CompositeOrderSummaryResult> createCompositeApiCallObservable() {
        // Request params
        SessionState sessionState = mNordstromSessionService.getSessionState();
        boolean isLoggedIn = mNordstromSessionService.isLoggedIn();

        // Order summary
        Observable<OrderSummary> orderSummaryObservable;
        if (isLoggedIn) {
            orderSummaryObservable = mCheckoutDAO.getRegisteredOrderSummary(sessionState);
        } else {
            orderSummaryObservable = mCheckoutDAO.getGuestOrderSummary(sessionState);
        }

        // Address book
        Observable<AddressBook> addressBookObservable;
        if (mUserSession.getAddressBook() != null || !isLoggedIn) {
            addressBookObservable = Observable.just(mUserSession.getAddressBook());
        } else {
            // only valid if logged in
            addressBookObservable = mAccountDAO.getAddresses(sessionState);
        }

        // Payment methods
        Observable<PaymentMethods> paymentMethodsObservable;
        if (mUserSession.getPaymentMethods() != null || !isLoggedIn) {
            paymentMethodsObservable = Observable.just(mUserSession.getPaymentMethods());
        } else {
            paymentMethodsObservable = mAccountDAO.getPaymentMethods(sessionState, "true");
        }

        // Combine requests for async dispatch
        Observable<CompositeOrderSummaryResult> compositeOrderSummaryResultObservable =
                Observable.zip(orderSummaryObservable, addressBookObservable, paymentMethodsObservable, CompositeOrderSummaryResult::new);

        // Conditionally add loyalty
        if (mAppPreferences.getFeatureEnabledLoyalty()) {
            compositeOrderSummaryResultObservable = compositeOrderSummaryResultObservable.flatMap(compositeOrderSummaryResult ->
                    mLoyaltyCheckoutController.getRewardsPresentation(compositeOrderSummaryResult.mOrderSummary)
                            .onErrorReturn(throwable -> null)
                            .map(loyaltySessionState -> {
                                compositeOrderSummaryResult.setCheckoutRewardsSectionPresentation(loyaltySessionState);
                                return compositeOrderSummaryResult;
                            }));
        }

        return compositeOrderSummaryResultObservable;
    }

    private Observable<SubmitOrderResponse> getSubmitOrderObservable() {
        CVV cvv = getCvv(mOrderSummary.getCreditCard());
        if (cvv != null && cvv.getCreditCardIdentifier() != null && !cvv.getCreditCardIdentifier().isEmpty()) {
            if (mNordstromSessionService.isLoggedIn()) {
                return mCheckoutDAO.postRegisteredSubmitOrderWithCvv(mNordstromSessionService.getSessionState(), cvv);
            } else {
                return mCheckoutDAO.postSubmitOrderWithCvv(mNordstromSessionService.getSessionState(), cvv);
            }
        } else {
            // purchasing solely with Nordstrom notes or gift cards, doesn't need card CVV
            if (mNordstromSessionService.isLoggedIn()) {
                return mCheckoutDAO.postRegisteredSubmitOrder(mNordstromSessionService.getSessionState());
            } else {
                return mCheckoutDAO.postSubmitOrder(mNordstromSessionService.getSessionState());
            }
        }
    }

    private void routeToShippingFragment() {
        CheckoutShippingFragment checkoutShippingFragment = new CheckoutShippingFragment();
        mRouteChanger.routeChange(checkoutShippingFragment);
        String pageName = String.format(
                AnalyticsConstants.ANALYTICS_CHECKOUT_SHIPPING,
                isLoggedIn() ? AnalyticsConstants.ANALYTICS_REGISTERED : AnalyticsConstants.ANALYTICS_GUEST);

        mAnalytics.view(pageName, AnalyticsConstants.ANALYTICS_CATEGORY_ID_CHECKOUT);
    }

    private void updateUIForCreditCard() {

        boolean doAppliedGiftCardsAndNotesCoverNetTotal = mOrderSummaryPresentationHelper.doAppliedGiftCardsAndNotesCoverNetTotal(mOrderSummary);
        CreditCard currentCreditCard = mUserSession.getCurrentlySelectedCreditCard();
        if (mPaymentMethodPresentationHelper.shouldShowCvvFieldForSelectedCard(doAppliedGiftCardsAndNotesCoverNetTotal,
                currentCreditCard, mUserSession.getCreditCardCvv())) {
            // Limit cvv text length for card type
            int cvvLength = mPaymentMethodPresentationHelper.getCardType(currentCreditCard).getNumCvvDigits();
            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(cvvLength);
            mCvvEditText.setFilters(filterArray);
            mCvvEditText.setVisibility(View.VISIBLE);

            // Set the CVV if we know about it in the user session, but hide it from being edited - Any Q's about this? See Mr. Tim N.
            if (hasCvv(mUserSession.getCreditCardCvv())) {
                mCvvEditText.setText(mUserSession.getCreditCardCvv().getCreditCardIdentifier());
                mCvvEditText.setVisibility(View.GONE);
            }

        } else {
            mCvvEditText.setVisibility(View.GONE);
        }

        if (currentCreditCard == null || doAppliedGiftCardsAndNotesCoverNetTotal) {
            //hide mCheckoutPaymentCard
            mCheckoutAddCreditDebitLabel.setVisibility(View.VISIBLE);

        } else {
            mCheckoutAddCreditDebitLabel.setVisibility(View.GONE);
            mCheckoutPaymentCard.applyCardOrShowText(currentCreditCard, R.string.checkout_select_credit_card);
            mCheckoutPaymentCard.setCheckIconVisible(false);
        }

    }

    @Nullable
    private Address getUserDefaultAddress() {
        if (mUserSession.getAddressBook() != null && mUserSession.getAddressBook().getEntries() != null) {
            long defaultId = mUserSession.getAddressBook().getDefaultShipEntryId();
            List<Address> addressBook = mUserSession.getAddressBook().getEntries();
            for (Address address : addressBook) {
                if (mAddressPresentationHelper.getAddressIdSafe(address) == defaultId) {
                    return address;
                }
            }
        }
        return null;
    }

    /**
     * Update the UI with the order summary shipping address or user's default address in address book
     */
    private void updateUIForShippingAddress(Address address) {
        if (address != null) {
            mAddressComponentItem.applyAddress(address);
            mAddressComponentItem.setVisibility(View.VISIBLE);
            mAddShippingAddressTextView.setVisibility(View.GONE);
        } else {
            mAddressComponentItem.setVisibility(View.GONE);
            mAddShippingAddressTextView.setVisibility(View.VISIBLE);
        }
    }

    @Nullable
    private Address getShippingAddress() {
        if (mUserSession.hasValidShippingAddress()) {
            return mUserSession.getShippingAddress();
        } else {
            return getUserDefaultAddress();
        }
    }

    /**
     * Update the loyalty rewards section component with the initial loyalty session state.
     *
     * @param presentation the initial loyalty session state.
     */
    private void updateLoyaltyUi(@Nullable CheckoutRewardsSectionPresentation presentation) {
        if (presentation != null) {
            mCheckoutRewardsSectionComponent.setVisibility(View.VISIBLE);
            mCheckoutRewardsSectionComponent.applyLoyaltyStatus(presentation);
            mCheckoutRewardsSectionComponent.setListener(new CheckoutComponentClickListener());
        } else {
            mCheckoutRewardsSectionComponent.setVisibility(View.GONE);
        }
    }

    private void updateUi() {

        //Shipping Section
        Address address = getShippingAddress();
        mUserSession.setShippingAddress(address);
        updateUIForShippingAddress(address);

        //Credit Card Section
        updateUIForCreditCard();

        //Order Items Section
        updateOrderItems();

        //Invoice (totals) UI
        mSubtotalComponent.applyPresentation(mSubtotalComponentPresentationFactory.createPresentation(mOrderSummary));

        if (mOrderSummary.getPromotionDisplay() != null && !mOrderSummary.getPromotionDisplay().isEmpty()) {
            mCheckoutPromoDisplayText.setVisibility(View.VISIBLE);
            mCheckoutPromoDisplayText.setText(mOrderSummary.getPromotionDisplay());
        }
    }


    /**
     * For some situations, we need to show an additional fragment requesting contact
     * information before we can proceed with the checkout. If the following conditions are satisfied,
     * we show the shipping fragment
     * <p>
     * - If the cart contains a BOPUS item
     * - If we are logged in
     * - If there are no shipping addresses in our address book
     * - If the payment methods contains a gift card or NN
     */
    private boolean launchAdditionalContactAddressRequestIfNeeded() {

        if (mOrderSummary != null && mNordstromSessionService.isLoggedIn()) {

            if (!Boolean.TRUE.equals(mOrderSummary.getHasContactAddress())) {

                //Allow user to use their shipping address if they have one
                final Address shippingAddress = mAddressPresentationHelper.abbreviateState(getShippingAddress());

                if (shippingAddress != null) {
                    final Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.checkout_fragment_contact_address_dialog);
                    dialog.findViewById(R.id.checkout_dialog_use_shipping).setOnClickListener(new DialogUseShippingClickListener(dialog, shippingAddress));
                    dialog.findViewById(R.id.checkout_add_contact_address).setOnClickListener(new DialogAddContactAddressClickListener(dialog));
                    dialog.show();
                } else {
                    Intent intent = new Intent(getActivity(), CreateContactAddressActivity.class);
                    mActivityRouter.startActivityForResult(this, intent, ActivityRequestCodes.CREATE_CONTACT_ADDRESS_ACTIVITY);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Select the order's credit card for registered users.
     *
     * @param creditCard the card to select.
     */
    private void apiSetRegisteredCreditCardId(CreditCard creditCard) {
        if (creditCard != null && creditCard.getId() != null) {
            mApiDataFetchedByOnActivityResult = true;
            showLoadingIndicatorTransparent(getView());
            mCompositeSubscription.add(mCheckoutDAO.putRegisteredCreditCardId(mNordstromSessionService.getSessionState(), creditCard.getId())
                    .flatMap(response -> createCompositeApiCallObservable())
                    .observeOn(mScheduler.mainThread())
                    .subscribe(new CompositeOrderSummaryResultObserver()));
        }
    }

    private void updateOrderItems() {
        List<ItemsPerDeliveryMethod> itemsPerDeliveryMethods = mOrderSummaryPresentationHelper.splitOrderSummeryByShippingMethod(mOrderSummary);
        mPickupMethodContainer.removeAllViews();

        /**
         * If we have reached here it means we have no shipping methods that could be extracted
         * from the OrderSummary. This is likely due to the contents of the bag being non-shippable
         * (virtual gift cards, etc).
         */
        ArrayList<ShippingMethod> allShippingMethods = mOrderSummaryPresentationHelper.getAllAvailableShippingMethods(mOrderSummary);
        if (allShippingMethods.isEmpty()) {
            mShippingNotEligible.setVisibility(View.VISIBLE);
            mSelectShippingMethodTv.setVisibility(View.GONE);
            mShippingMethodCaret.setVisibility(View.INVISIBLE);
            mShippingMethodComponentItem.setVisibility(View.GONE);
            mCheckOutAddress.setVisibility(View.GONE);
            mShippingMethodsLayout.setOnClickListener(null);

        } else {
            ShippingMethod method = mOrderSummaryPresentationHelper.getSelectedShipMethod(mOrderSummary);
            if (method == null) {
                mSelectShippingMethodTv.setVisibility(View.VISIBLE);
            } else {
                mSelectShippingMethodTv.setVisibility(View.GONE);
                boolean isOrderEntirelyHolidayItems = mOrderSummaryPresentationHelper.isOrderEntirelyHolidayItems(mOrderSummary);
                mShippingMethodComponentItem.applyShippingMethod(method, true, isOrderEntirelyHolidayItems);
                mShippingMethodComponentItem.enableCheckIcon(false);
            }
        }

        for (ItemsPerDeliveryMethod method : itemsPerDeliveryMethods) {
            if (method.getStoreId() > 0l) {
                // Create view component for the store to pick-up in
                ReviewStoreItemsComponent view = new ReviewStoreItemsComponent(getActivity());
                view.updateFromItemsPerDelivery(method);
                mPickupMethodContainer.addView(view);

                view.setClickableAreaListener(new ItemsPerDeliveryClickListener(method));

            } else {

                View clickableArea = mProductImageClickableArea;
                OrderItemGridView productImagesGrid = mProductImages;
                View section = mShipToAddressSection;
                TextView methodTitle = mShippingItemsTitle;

                if (method.isEmailMethod()) {
                    clickableArea = mEmailProductImageClickableArea;
                    productImagesGrid = mEmailProductImages;
                    section = mEmailLayout;
                    methodTitle = mEmailItemsLayoutTitle;
                }

                int orderItemsCount = 0;

                // Update the shipping-to-address area
                if (method.getItems().size() == 0) {
                    section.setVisibility(View.GONE);
                    clickableArea.setOnClickListener(null);
                } else {
                    section.setVisibility(View.VISIBLE);

                    List<AbstractItem> productImages = new ArrayList<>();

                    for (AbstractItem item : method.getItems()) {
                        orderItemsCount += item.getQuantity();
                        productImages.add(item);
                    }

                    productImagesGrid.setProductImages(productImages);
                    clickableArea.setOnClickListener(new ItemsPerDeliveryClickListener(method));
                }

                int stringId;
                if (method.isEmailMethod()) {
                    stringId = method.getItems().size() == 1 ? R.string.checkout_emailing_item_single_title : R.string.checkout_emailing_item_plural_title;
                } else {
                    stringId = orderItemsCount == 1 ? R.string.checkout_shipping_item_single_title : R.string.checkout_shipping_item_plural_title;
                }

                String message = getString(stringId);
                methodTitle.setText(String.format(message, String.valueOf(orderItemsCount)));
            }
        }
    }

    /**
     * Handle the case of an error on order submission.
     *
     * @param error an error, likely a Retrofit/API error.
     */
    @VisibleForTesting
    void onSubmitOrderError(Throwable error) {
        Log.e(TAG, "Failed to submit order", error);

        // oh no! Checkout Transaction Failed
        mCheckoutTransaction.fail();

        String errorMessage = ErrorResponseUtil.getApiErrorMessage(error);
        if (errorMessage != null) {
            // TODO - standardize hard-coded, weird server errors.
            final String genericMessage = "The creator of this fault did not specify a Reason".toLowerCase(Locale.US);
            if (errorMessage.toLowerCase(Locale.US).contains(genericMessage))
                errorMessage = getString(R.string.checkout_friendly_error_general);

            mNordstromErrorMessengerManager.showErrorMessengerWithText(errorMessage);
        } else {
            mNordstromSnackbarManager.showIndefinite(R.string.toast_connectivity_issues);
        }

        //
        // If we're a debug build fire coreMetrics shop9/order events so we can see them in ITT tool  since lower envs aren't reliable
        //
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            mCheckoutAnalyticsDispatch.coreMetricsFireAction9(mOrderSummary);
            mCheckoutAnalyticsDispatch.checkoutSubmitOrder(mOrderSummary);
        }
        mCheckoutAnalyticsDispatch.checkoutSubmitOrderFailed(mOrderSummary, errorMessage);

        // In the case of error, we clear the CVV just in case the issue was an incorrect CVV added
        // in an add-credit-card page previously
        clearCvv();
    }

    /**
     * Handles the case of a successful order submission.
     *
     * @param response the submit order response.
     */
    @VisibleForTesting
    void onSubmitOrderResponse(SubmitOrderResponse response) {
        mUserSession.setSuccessfulOrderResponse(response);
        mOrderSummary.setOrderNumber(response.getOrderNumber());

        if (!mNordstromSessionService.isLoggedIn()) {
            mUserSession.clearGuestBillingAndShippingData();
        }
        mCheckoutAnalyticsDispatch.coreMetricsFireAction9(mOrderSummary);
        mCheckoutAnalyticsDispatch.checkoutSubmitOrder(mOrderSummary);
        mCheckoutAnalyticsDispatch.eventNotesAppliedToOrderSubmission(mOrderSummary);

        // Yay! Checkout Transaction Success
        mCheckoutTransaction.end();

        BusProvider.getInstance().post(new CartChangedEvent());

        String loyaltyWarningMessage = mLoyaltyDomain.getThankYouPageLoyaltyWarningMessage(response);
        mRouteChanger.routeChange(CheckoutThankYouFragment.newInstance(loyaltyWarningMessage));
    }

    /**
     * In the case of error, clear CVV and re-update views.
     */
    @VisibleForTesting
    void clearCvv() {

        // Clear the cvv and re-do the credit card ui
        mUserSession.setCreditCardCvv(null);
        updateUIForCreditCard();

        if (mCvvEditText.getVisibility() == View.VISIBLE) {
            mCvvEditText.setText("");
            mCvvEditText.requestFocus();
        }
    }

    /**
     * Simple condition for determining if a CVV is valid.
     *
     * @param cvv credit card id
     * @return true if the cvv number exists and isn't empty
     */
    @VisibleForTesting
    static boolean hasCvv(@Nullable CVV cvv) {
        return cvv != null && !StringUtils.isNullOrEmpty(cvv.getCreditCardIdentifier());
    }
    //endregion

    //region ACCESSORS -----------------------------------------------------------------------------
    //endregion

    //region INNER CLASSES -------------------------------------------------------------------------

    @VisibleForTesting
    static class CompositeOrderSummaryResult {

        OrderSummary mOrderSummary;
        AddressBook mAddressBook;
        PaymentMethods mPaymentMethods;
        CheckoutRewardsSectionPresentation mCheckoutRewardsSectionPresentation;

        CompositeOrderSummaryResult(OrderSummary orderSummary,
                                    AddressBook addressBook,
                                    PaymentMethods paymentMethods) {
            mOrderSummary = orderSummary;
            mAddressBook = addressBook;
            mPaymentMethods = paymentMethods;
        }

        public void setCheckoutRewardsSectionPresentation(CheckoutRewardsSectionPresentation presentation) {
            mCheckoutRewardsSectionPresentation = presentation;
        }
    }

    //endregion

    //region OBSERVERS -----------------------------------------------------------------------------

    @VisibleForTesting
    class ActivityResultObserver implements Observer<ActivityResult> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(ActivityResult activityResult) {
            if (activityResult.getRequestCode() == ActivityRequestCodes.SIGN_IN_ACTIVITY.ordinal()) {
                mCheckoutRewardsSectionComponent.setVisibility(View.GONE);
            }
        }
    }

    private class SubmitOrderResponseObserver implements Observer<SubmitOrderResponse> {
        @Override
        public void onCompleted() {
            hideLoadingIndicatorIgnoreCount();
        }

        @Override
        public void onError(Throwable e) {
            hideLoadingIndicatorIgnoreCount();
            onSubmitOrderError(e);
        }

        @Override
        public void onNext(SubmitOrderResponse response) {
            onSubmitOrderResponse(response);
        }
    }

    private class CompositeOrderSummaryResultObserver implements Observer<CompositeOrderSummaryResult> {
        @Override
        public void onCompleted() {
            hideLoadingIndicator();
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, "Failed to get payment methods", e);
            hideLoadingIndicator();

            String errorMessage = ErrorResponseUtil.getApiErrorMessage(e);
            if (errorMessage != null) {
                mNordstromErrorMessengerManager.showErrorMessengerWithText(errorMessage);
            } else {
                mNordstromSnackbarManager.showIndefinite(R.string.toast_connectivity_issues);
            }
        }

        @Override
        public void onNext(CompositeOrderSummaryResult result) {
            mUserSession.setPaymentMethods(result.mPaymentMethods);
            mRdsfSession.updateStateWithPayments(result.mPaymentMethods);
            mUserSession.setAddressBook(result.mAddressBook);
            updateToOrderSummary(result.mOrderSummary);
            updateUi();
            updateLoyaltyUi(result.mCheckoutRewardsSectionPresentation);
        }
    }

    /**
     * Observable on the place order confirmation that submits the order if true.
     */
    private class ShouldSubmitOrderObservable implements Observer<Boolean> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Boolean shouldSubmit) {
            if (shouldSubmit) {
                // perform the checkout
                showLoadingIndicatorOpaque(R.string.checkout_placing_order_indicator_message);
                mCompositeSubscription.add(getSubmitOrderObservable().observeOn(mScheduler.mainThread())
                        .subscribe(new SubmitOrderResponseObserver()));
            }
        }
    }

    //endregion

    //region PUBLIC CLASS METHODS ------------------------------------------------------------------
    //endregion

}
