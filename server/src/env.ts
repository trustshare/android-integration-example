import * as dotenv from 'dotenv';
dotenv.config();

export const env = {
  TRUSTSHARE_PRIVATE_API_KEY: process.env.TRUSTSHARE_PRIVATE_API_KEY,
  TRUSTSHARE_PUBLIC_API_KEY: process.env.TRUSTSHARE_PUBLIC_API_KEY,
};
