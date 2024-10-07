import csv
import os
import random
from multiprocessing import Process

def generate_name():
    first_names = ["Ana", "Bruno", "Carlos", "Daniela", "Eduardo", "Fernanda", "Gabriel", "Helena", "Igor", "Juliana"]
    last_names = ["Silva", "Souza", "Lima", "Pereira", "Oliveira", "Santos", "Gomes", "Almeida", "Costa", "Rocha"]
    return f"{random.choice(first_names)} {random.choice(last_names)}"

def generate_email(name):
    domain = "example.com"
    return f"{name.lower().replace(' ', '.')}@{domain}"

def generate_phone():
    return f"{random.randint(100000000, 999999999)}"

def write_csv(output_file, total_rows):
    with open(output_file, 'a', newline='', encoding='utf-8') as f:
        writer = csv.writer(f, delimiter=';')
        for _ in range(total_rows):
            name = generate_name()
            email = generate_email(name)
            phone = generate_phone()
            writer.writerow([name, email, phone])

def main():
    output_file = 'dados.csv'
    total_size = 10 * 1024 * 1024  
    num_workers = 8 
    estimated_row_size = 50  
    total_rows = total_size // estimated_row_size 

    with open(output_file, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerow(['nomes', 'email', 'numero_de_telefone'])

    rows_per_worker = total_rows // num_workers  

    processes = []
    
    for _ in range(num_workers):
        p = Process(target=write_csv, args=(output_file, rows_per_worker))
        p.start()
        processes.append(p)

    for p in processes:
        p.join()

    print(f"Arquivo {output_file} gerado com {os.path.getsize(output_file)} bytes.")

if __name__ == "__main__":
    main()

