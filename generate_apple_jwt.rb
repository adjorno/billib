require 'jwt'
require 'openssl'

# --- Configuration ---
KEY_ID      = '58PZHD4PW3'       # Your 10-character Key ID from Apple Developer
TEAM_ID     = '7XHUV5UUCP'       # Your 10-character Team ID
P8_FILE_PATH = 'MusicAPI_AuthKey_58PZHD4PW3.p8'

# 1. Load the Private Key from the .p8 file
# .p8 files use the PKCS#8 format which OpenSSL handles easily
private_key_content = File.read(P8_FILE_PATH)
private_key = OpenSSL::PKey::EC.new(private_key_content)

# 2. Define the Header (Required for Apple Music API)
# The 'kid' tells Apple which public key to use to verify the signature
header = {
  alg: 'ES256',
  kid: KEY_ID
}

# 3. Define the Payload (Claims)
# iat: Issued At (Current time)
# exp: Expiration (Apple allows up to 6 months / 15,777,000 seconds)
payload = {
  iss: TEAM_ID,
  iat: Time.now.to_i,
  exp: Time.now.to_i + (180 * 24 * 60 * 60) # 180 days
}

# 4. Generate the JWT
token = JWT.encode(payload, private_key, 'ES256', header)

puts "--- Your Apple Music JWT ---"
puts token
