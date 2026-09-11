# Local test providers

`seed-demo-providers.sql` adds three fictitious providers and one service per
provider: air conditioning, house cleaning, and event photography. Together with
the existing demo plumber, the homepage can display four providers.

Run only against the local development database after the Flyway migrations:

```powershell
psql -X -h localhost -p 5434 -U linkops -d linkops -v ON_ERROR_STOP=1 -f linkops-api/scripts/seed-demo-providers.sql
```

Supply the local database password when prompted. The script is transactional
and can be rerun without duplicating or overwriting existing profiles. It is
deliberately outside Flyway so deploying the application does not add test users
to production.

The new profiles have random, undisclosed passwords, no ratings, no completed
jobs, and no verification badge.

To attach the generated demonstration photographs, run the separate local-only
media seed after the provider seed:

```powershell
psql -X -h localhost -p 5434 -U linkops -d linkops -v ON_ERROR_STOP=1 -f linkops-api/scripts/seed-demo-media.sql
```

The images live in `frontend/public/images/`. Their exact prompts and provenance
are recorded in `frontend/public/images/DEMO-PHOTOS.md`. The homepage displays
cropped versions of those same photos as demo avatars; uploaded photos take
precedence. Neither seed changes real customers or adds fabricated reviews.
