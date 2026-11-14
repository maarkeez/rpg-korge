# The mailing list is kept by Brevo, not the telemetry collector

The mailing list is kept by Brevo, on its free plan, hosted in the EU: Wishlist, the gold button at the bottom of the
main menu, opens Brevo's signup form, in English, in the browser, and the game never sees the email. Brevo confirms
each email with double opt-in, keeps it, lets the player unsubscribe, and is what a new demo or a new release would be
announced from. We chose it because the game is a free pet project that must cost nothing to run, and an email,
unlike the random ids of telemetry, says who a player is: the least of it in our own systems the better.

## Considered options

- **The telemetry collector, on Cloudflare**: we already run it, but Cloudflare has no mailing list, and its Email
  Sending, in public beta, needs the paid Workers plan and a domain of our own, which the game, on `github.io` and
  `workers.dev`, doesn't have. We would also write the signup form, the confirmation email, unsubscribing and deleting.
- **An email field in the game**, posting to Brevo: the player never leaves the game, but KorGE has no text entry we
  use yet, on phone keyboards neither.
- **MailerLite** and **CleverReach**: free and in the EU too, but their free plans stop at about 500 and 250 contacts,
  while Brevo's has no limit on contacts, only 300 emails a day.

## Consequences

- Opening Wishlist records no product event: the privacy notice names every product event, so adding one would ask
  every player for consent again, and Brevo already counts the visits to its form and the signups.
- What the mailing list is for, that Brevo keeps it, how to unsubscribe, and the repository's GitHub issues as the
  contact, are written on the signup form, not in the game's privacy notice, which stays at version 2.
- The email footer Brevo requires when sending, with the sender's postal address, is only needed for the first
  announcement, not to collect emails.
