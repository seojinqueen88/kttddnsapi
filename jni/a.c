int user_data_set_instant_password(int year, int month,int day, int hour , char *mac_address, char *new_password, char *old_password) {
	int i;
	unsigned int passwd_key = 0;
	unsigned int old_passwd_key = 0;
	unsigned int new_passwd_key = 0;
	char str[16];
	size_t len;
	char key[16];
	char pwd[8];
	int y, m, d, h;
	 
	unsigned char mac[8];
	const char  pw_symbol[] = {'_','+','-','=','?','@','~','!','&','#'};
   
	y =  (year);
	m =  (month);
	d =  (day);
	h =  (hour);

    for(i = 0 ; i < 6 ; i++)
	{
		passwd_key = passwd_key + (unsigned char)mac[i];
	}
	//////////////////// OLD PW MAKE ////////////////////
	old_passwd_key = passwd_key * y * m * d * (h == 0? 24:h);
	sprintf(str, "%x", old_passwd_key);
	len = strlen(str);
	memset(str, 0, sizeof(str));
	for(i = 0 ; i < 8 ; i++)
	{
		str[i] = (unsigned char)(old_passwd_key >> ((i%len) * 4)) & 0xf;
		key[i] = (unsigned char)(mac[(11-i)/2] >> ((i%2)*4)) & 0xf;
		pwd[i] = (unsigned char)(str[i] + key[i]) & 0xf;
	}
	memset(old_password,0,sizeof(pwd));
	sprintf(old_password,"%x%x%x%x%x%x%x%x",pwd[7],pwd[6],pwd[5],pwd[4],pwd[3],pwd[2],pwd[1],pwd[0]);
	old_password[8] = 0;

	//////////////////// NEW PW MAKE ////////////////////
	new_passwd_key = passwd_key * y * m * d * (25 - h);
	sprintf(str, "%x", new_passwd_key);
	len = strlen(str);
	memset(str, 0, sizeof(str));
	for(i = 0 ; i < 8 ; i++)
	{
		str[i] = (unsigned char)(new_passwd_key >> ((i%len) * 4)) & 0xf;
		key[i] = (unsigned char)(mac[(11-i)/2] >> ((i%2)*4)) & 0xf;
		pwd[i] = (unsigned char) (str[i] + key[i]) & 0xf;
	}
	memset(new_password,0,sizeof(pwd));
	sprintf(new_password,"%x%x%x%x%x%x%x%x",pwd[7],pwd[6],pwd[5],pwd[4],pwd[3],pwd[2],pwd[1],pwd[0]);
	new_password[7] = pw_symbol[new_passwd_key % 10];
	new_password[8] = 0;

	return 0;
}

int main()
{
    char * mac ={0x00,0x00,0xC2,0x8F,0xFF,0xFF}; 
    char n[32];
    char o[32];
    user_data_set_instant_password(2022,9,13,17,mac, n,o);
    printf(n);
}