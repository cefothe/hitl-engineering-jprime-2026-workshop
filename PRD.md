# PRD: Reliable Webhook Processing for Payment Events

**Author:** Maria K. (Payments PM)
**Status:** Draft — ready for engineering review
**Reviewers:** Backend team, Finance, Customer Support

---

## Background

We integrated with PayHub (our payment provider) three months ago and the integration is now our biggest source of customer pain. Last week:

- 14 support tickets from customers who paid but show as unpaid in our system
- 3 confirmed cases where a subscriber was double-charged because we thought their first payment had failed
- Finance flagged a €2,400 discrepancy between PayHub's dashboard and our internal reports for October

The pattern is consistent: PayHub sends us events (`payment.succeeded`, `subscription.canceled`, `refund.created`, etc.) over HTTPS, and our current handler loses some, occasionally processes others twice, and goes down completely when PayHub sends a burst after one of their own outages.

This is starting to hurt commercial conversations — two of our enterprise prospects asked about our webhook SLA in the last sales cycle, and we don't have one.

## Problem statement

When PayHub sends us an event, we need to apply it to our system exactly once, even when things go wrong on either side. Right now we can't promise that, and we can't even answer the simpler question *"did we receive event X?"* without an engineer grepping logs.

## Goals (v1)

1. No payment event is ever lost, even if our service restarts mid-processing.
2. No payment event is ever applied twice, even if PayHub sends it twice (they do).
3. Support can answer *"did we receive event X, and what happened to it?"* in under 30 seconds, without an engineer.
4. We can survive PayHub sending us a backlog (estimated burst: ~5,000 events in 5 minutes after their outages) without dropping events or taking down the rest of the app.
5. If we can't process an event automatically, it ends up somewhere a human can look at it and decide what to do — it does not get silently dropped.

## Non-goals

- Webhooks from any provider other than PayHub.
- A UI for support. A CLI or admin endpoint is fine for v1.
- Replaying historical events from before this launches — we'll clean up the existing mess as a one-off.
- Notifying end customers when events arrive (existing email flow handles that).

## Functional requirements

- We accept `POST` requests from PayHub at a public endpoint.
- We confirm the event genuinely came from PayHub before doing anything with it. PayHub's docs describe how — engineering will know what to do.
- We acknowledge receipt to PayHub quickly. Their docs say they'll consider the delivery failed and retry if we take longer than 5 seconds.
- Each event eventually updates the right thing in our system: payments, subscriptions, or refunds.
- Events that fail processing are retried automatically. If they keep failing, they're set aside for review.
- Support has a way to look up an event by PayHub's event ID and see: did we receive it, when, and what's its current state.

## User stories

> As a **customer**, when I pay successfully, my account reflects that payment within a few minutes, every time.

> As a **support agent**, when a customer says *"my payment isn't showing,"* I can check whether PayHub even told us about it, without pinging engineering.

> As a **finance analyst**, I trust that the events PayHub sent us match what's in our database, so my monthly reconciliation isn't a manual diff.

> As an **on-call engineer**, when something goes wrong, I can replay or skip individual events without writing a one-off script.

## Success metrics

- Webhook-related support tickets: from ~5/week to 0.
- 99.9% of events fully processed within 5 minutes of arrival at our endpoint.
- Finance reconciliation discrepancy under €50/month.
- Zero engineering tickets per week with the title *"can you check if we got event…"*

## Open questions for engineering

- How long do we need to keep the raw event payloads? Compliance hasn't given me a firm answer yet — I'll come back on this, please assume *"a while"* for now.
- Do we need on-call paging when an event has failed too many times, or is a daily digest enough for v1?
- PayHub's docs mention they sometimes send events out of order. Is that something we need to handle, or does it not matter for our use case?

## Timeline

We'd like this in production within 3 weeks. The finance reconciliation pain is real and the next monthly close is in 4.
