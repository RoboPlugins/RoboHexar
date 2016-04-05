package com.nordstrom.app.checkout.controller;

import android.support.annotation.Nullable;

import com.nordstrom.app.checkout.component.CheckoutRewardsSectionComponent;
import com.nordstrom.app.main.util.PhoneNumberFormatter;
import com.nordstrom.app.paymentmethods.presentation.PaymentMethodPresentationHelper;
import com.nordstrom.domain.rewards.LoyaltyDomain;
import com.nordstrom.domain.rewards.model.LoyaltySessionState;
import com.nordstrom.domain.rewards.presentation.CheckoutRewardsSectionPresentation;
import com.nordstrom.services.checkout.model.OrderSummary;
import com.nordstrom.services.paymentmethods.model.CreditCard;
import com.nordstrom.services.rewards.model.LoyaltyStatus;

import javax.inject.Inject;

import rx.Observable;

/**
 * Controls loyalty logic for the {@link com.nordstrom.app.checkout.fragment.CheckoutFragment}, mostly for driving
 * the presentation to the {@link CheckoutRewardsSectionComponent}.
 */
public class LoyaltyCheckoutController {

    //region INJECTED CLASSES ----------------------------------------------------------------------

    private final LoyaltyDomain mLoyaltyDomain;
    private final PhoneNumberFormatter mPhoneNumberFormatter;
    private final PaymentMethodPresentationHelper mPaymentMethodPresentationHelper;

    //endregion

    //region CLASS VARIABLES -----------------------------------------------------------------------

    private Boolean mUserEnrollable = null;
    private LoyaltyStatus mUserEnrolledStatus = null;

    //endregion

    //region CONSTRUCTOR ---------------------------------------------------------------------------

    @Inject
    public LoyaltyCheckoutController(LoyaltyDomain loyaltyDomain,
                                     PhoneNumberFormatter phoneNumberFormatter,
                                     PaymentMethodPresentationHelper paymentMethodPresentationHelper) {

        mLoyaltyDomain = loyaltyDomain;
        mPhoneNumberFormatter = phoneNumberFormatter;
        mPaymentMethodPresentationHelper = paymentMethodPresentationHelper;
    }


    //endregion

    //region ACCESSORS -----------------------------------------------------------------------------
    //endregion

    //region PUBLIC CLASS METHODS ------------------------------------------------------------------

    /**
     * Get the rewards presentation observable for checkout {@link CheckoutRewardsSectionComponent}.
     * Note: This will Post loyalty association for an order the first time it is fetched, and possibly subsequent
     * times if not newly enrolled.
     *
     * @param orderSummary the checkout order summary.
     * @return the presentation.
     */
    public Observable<CheckoutRewardsSectionPresentation> getRewardsPresentation(final OrderSummary orderSummary) {
        final boolean usingNordstromTender = mPaymentMethodPresentationHelper.isNordstromTender(orderSummary.getCreditCard());

        if (mUserEnrolledStatus != null && !usingNordstromTender) {
            // Maintain the newly enrolled presentation with the status that was provided, but that is trumped by tender association.
            return Observable.just(new CheckoutRewardsSectionPresentation(
                    CheckoutRewardsSectionPresentation.CheckoutLoyaltyState.NEWLY_ASSOCIATED,
                    getFirstName(mUserEnrolledStatus), getRewardsNumber(mUserEnrolledStatus)));
        }
        return mLoyaltyDomain.postOrderAssociation(orderSummary).map(sessionState -> {
            if (mUserEnrollable == null) {
                // Keep track of the enroll-ability state on the first call
                mUserEnrollable = !sessionState.isTenderNeutralAssociated();
            }

            if (usingNordstromTender) {
                // Favor the nordstrom tender association over others
                return new CheckoutRewardsSectionPresentation(
                        CheckoutRewardsSectionPresentation.CheckoutLoyaltyState.PREVIOUSLY_OR_TENDER_ASSOCIATED,
                        getFirstName(orderSummary.getCreditCard()), "");
            } else {
                return createCheckoutRewardsSectionPresentation(sessionState);
            }
        });
    }

    /**
     * Inform the controller that the user has newly enrolled with the given status response. Ensure that a call to
     * getRewardsPresentation occurs before this.
     *
     * @param status a {@link LoyaltyStatus} response from a new association.
     */
    public void userEnrolledWithStatus(LoyaltyStatus status) {
        if (Boolean.TRUE.equals(mUserEnrollable)) {
            mUserEnrolledStatus = status;
        }
    }

    //endregion

    //region PRIVATE METHODS -----------------------------------------------------------------------

    private CheckoutRewardsSectionPresentation.CheckoutLoyaltyState getCheckoutLoyaltyState(LoyaltySessionState loyaltySessionState) {
        if (Boolean.FALSE.equals(mUserEnrollable) || loyaltySessionState.hasNordstromTenderForCurrentOrder()) {
            return CheckoutRewardsSectionPresentation.CheckoutLoyaltyState.PREVIOUSLY_OR_TENDER_ASSOCIATED;
        } else if (loyaltySessionState.isTenderNeutralAssociated()) {
            return CheckoutRewardsSectionPresentation.CheckoutLoyaltyState.NEWLY_ASSOCIATED;
        } else {
            return CheckoutRewardsSectionPresentation.CheckoutLoyaltyState.UNASSOCIATED;
        }
    }

    /**
     * Create {@link CheckoutRewardsSectionPresentation} for the current current session state.
     *
     * @return the created presentation.
     */
    private CheckoutRewardsSectionPresentation createCheckoutRewardsSectionPresentation(LoyaltySessionState loyaltySessionState) {
        LoyaltyStatus status = loyaltySessionState.getLoyaltyStatus();
        return new CheckoutRewardsSectionPresentation(getCheckoutLoyaltyState(loyaltySessionState), getFirstName(status), getRewardsNumber(status));
    }

    /**
     * Get the first name from a credit card.
     * @param card the card.
     * @return the card first name.
     */
    private String getFirstName(@Nullable CreditCard card) {
        if (card != null && card.getCardholderName() != null) {
            return card.getCardholderName().getFirstName();
        }
        return null;
    }

    /**
     * Get the first name from the loyalty status.
     * @param status the status.
     * @return the status first name.
     */
    private String getFirstName(@Nullable LoyaltyStatus status) {

        if (status != null) {
            return status.getFirstName();
        }
        return null;
    }

    private String getRewardsNumber(LoyaltyStatus status) {
        if (status != null) {
            return mPhoneNumberFormatter.formatNumber(status.getPhoneNumber());
        }
        return null;
    }

    //endregion

    //region OBSERVERS -----------------------------------------------------------------------------
    //endregion

    //region INNER CLASSES -------------------------------------------------------------------------
    //endregion
}
