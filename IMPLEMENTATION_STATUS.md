# Astra implementation checklist

This checklist tracks the Advanced Requirements document. Docker is intentionally excluded per the project request. A capability is marked complete only when both its API behavior and user flow are implemented; provider-backed flows still need credentials and end-to-end verification.

## Implemented

- [x] Authenticated storefront and admin APIs; refresh-token rotation, logout revocation, remember-me persistence, and basic per-IP authentication throttling.
- [x] Product DTOs, catalog filtering with URL state, cart, wishlist, compare, and checkout idempotency.
- [x] Saved customer addresses and checkout address selection.
- [x] Coupon redemption caps globally and per customer, recorded with the order transaction.
- [x] Customer order history and admin order search/status filtering with server-side pagination; dashboard uses a count endpoint.
- [x] Stock adjustment audit history and admin customer activation controls.
- [x] Verified-purchase reviews with admin approval/hide controls; public ratings only include published reviews.
- [x] Razorpay Orders API checkout, browser signature verification plus provider captured-status check, signed captured-payment webhook, and customer retry from My Orders.
- [x] Local setup documentation, placeholder environment examples, frontend checks, and CI build/verification workflow.

## Still required from the document

- [ ] Razorpay refunds, refund status reconciliation, and full recovery for late/ambiguous payment failures. Payment code is not live-tested because merchant credentials are not available.
- [ ] Stock reservations with expiry/release for unpaid checkouts; current checkout decrements stock when the order is placed.
- [ ] Order lifecycle email/in-app notifications and notification preferences.
- [ ] Abandoned-cart reminders and scheduled cleanup.
- [ ] Loyalty points/referrals, personalized recommendations, and fraud/risk rules.
- [ ] Product variant selection carried consistently through cart, checkout, order, and inventory.
- [ ] Category management, richer search facets/autocomplete, and merchandising controls.
- [ ] Admin analytics beyond basic counts, bulk CSV operations, and customer/order exports.
- [ ] Deployment/release pipeline, production monitoring/alerts, backups, and rollback runbook.
- [ ] Full end-to-end browser checks, accessibility review, and localization coverage.

## Verification notes

- Frontend lint, unit tests, and production build have passed during implementation. The initial JavaScript chunk is now below the prior 500 KB warning threshold through route splitting.
- Backend Java/test compilation and 12 focused payment/webhook, review, address, and coupon tests have passed.
- A full Spring Boot integration run against MySQL and live payment-provider verification have not been performed in this environment.
