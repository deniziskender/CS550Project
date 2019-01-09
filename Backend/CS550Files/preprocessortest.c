#define peripheral "led"
#define peripheral_type "gpio"
#define led_mem_start 0
#define led_mem_end 3

#define peripheral "sw"
#define peripheral_type "gpio"
#define sw_mem_start 4
#define sw_mem_end 7

int * periph_addr = (int *) 7;
int * periph_data = (int *) 8;

int gpio_write(int addr_start, int addr_offset, int data){

	*periph_addr = (addr_start + addr_offset);
	*periph_data = data;

	return 0;
}

int gpio_read(int addr_start, int addr_offset){

	*periph_addr = (addr_start + addr_offset);

	return *periph_data;
}

int main(){

	int led_data = 5;
	int sw_data;

	gpio_write(led_mem_start, 0, led_data);
	sw_data = gpio_read(sw_mem_start, 0);

	return 0;
}

