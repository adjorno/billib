# How to make https

https://www.digitalocean.com/community/tutorials/how-to-secure-nginx-with-let-s-encrypt-on-ubuntu-16-04

https://github.com/dokku/dokku-letsencrypt

It needs to run 
`dokku ps:rebuild yourapp` to apply new domains
`dokku proxy:ports-add yourapp http:80:5000` to set ports again

Once SSL is on turn off http traffic:
`dokku proxy:ports-remove yourapp http:80:5000`

# Cert Renewal

`dokku proxy:ports-add yourapp http:80:5000` to set ports again
`dokku letsencrypt:auto-renew yourapp`
`dokku proxy:ports-remove yourapp http:80:5000`

Restart the app