const { Issuer, Strategy } = require('openid-client');
const passport = require('passport');
const express = require('express');
const expressSession = require('express-session');
const bodyParser = require('body-parser');

let server;
exports.startServer = async function (spy) {
  const app = express();
  app.use(bodyParser.json());
  if (spy !== undefined) {
    app.use(spy);
  }

  const keycloakIssuer = await Issuer.discover('https://auth.rezoleo.fr/realms/rezoleo');

  const client = new keycloakIssuer.Client({
    client_id: 'nodejs-test',
    client_secret: '6XbU7XknaCk96wRyQZichLxvrPW608BP',
    redirect_uris: ['http://localhost:3000/auth/callback'],
    post_logout_redirect_uris: ['http://localhost:3000/logout/callback'],
    response_types: ['code'],
  });

  const memoryStore = new expressSession.MemoryStore();
  app.use(expressSession({ secret: 'secret', resave: false, saveUninitialized: true, store: memoryStore }));

  app.use(passport.initialize());
  app.use(passport.authenticate('session'));

  let id_token;
  passport.use(
    'oidc',
    new Strategy({ client }, (tokenSet, userInfo, done) => {
      id_token = tokenSet.id_token;
      return done(null, tokenSet.claims());
    })
  );
  passport.serializeUser((user, done) => {
    done(null, user);
  });
  passport.deserializeUser((user, done) => {
    done(null, user);
  });

  // callback always routes to test
  app.get('/auth/callback', (req, res, next) => {
    passport.authenticate('oidc', {
      successRedirect: '/testauth',
      failureRedirect: '/',
    })(req, res, next);
  });

  // function to check whether user is authenticated, req.isAuthenticated is populated by password.js
  // use this function to protect all routes
  var checkAuthenticated = (req, res, next) => {
    if (req.isAuthenticated()) {
      return next();
    }
    res.redirect('/test');
  };

  app.get('/testauth', checkAuthenticated, (req, res) => {
    res.send('authenticated');
  });

  app.get('/other', checkAuthenticated, (req, res) => {
    res.send('other');
  });

  //unprotected route
  app.get('/', function (req, res) {
    res.send('index');
  });

  // start logout request
  app.get('/logout', (req, res) => {
    res.redirect(client.endSessionUrl() + `&id_token_hint=${id_token}`);
  });

  // logout callback
  app.get('/logout/callback', (req, res) => {
    // clears the persisted user from the local storage
    req.logout(function (err) {
      if (err) {
        return next(err);
      }
      res.redirect('/');
    });
  });

  app.patch('/patch-user', (req, res) => {
    console.log(`MUST PATCH USER: ${req.body}`);
    res.status(200).send();
  });

  server = app.listen(3000, function () {
    console.log('Listening at http://localhost:3000');
  });
};

exports.closeServer = async () => await server.close();
