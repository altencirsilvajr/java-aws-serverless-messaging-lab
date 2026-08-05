import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { CommandApiService, ProcessingRecord, QueueStatus } from './command-api.service';

@Component({
  selector: 'app-root',
  imports: [ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly api = inject(CommandApiService);
  private readonly destroyRef = inject(DestroyRef);
  private pollHandle?: ReturnType<typeof setInterval>;

  readonly command = signal<ProcessingRecord | null>(null);
  readonly queues = signal<QueueStatus>({ pending: 0, deadLetter: 0 });
  readonly busy = signal(false);
  readonly error = signal<string | null>(null);
  readonly duplicateSubmission = signal(false);
  readonly form = new FormGroup({
    messageId: new FormControl(`demo-${Date.now()}`, { nonNullable: true, validators: [Validators.required] }),
    payload: new FormControl('rebuild-customer-index', { nonNullable: true, validators: [Validators.required] }),
    simulateFailure: new FormControl(false, { nonNullable: true }),
  });

  constructor() {
    this.destroyRef.onDestroy(() => this.stopPolling());
    this.refreshQueues();
  }

  submit(): void {
    if (this.form.invalid) return;
    this.busy.set(true);
    this.error.set(null);
    const value = this.form.getRawValue();
    this.api.submit(value.messageId, value.payload, value.simulateFailure)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: response => {
          this.command.set(response.command);
          this.duplicateSubmission.set(response.duplicate);
          this.startPolling(response.command.messageId);
        },
        error: () => this.error.set('Não foi possível publicar o comando.'),
      });
  }

  sendTransportDuplicate(): void {
    const current = this.command();
    if (!current) return;
    this.api.duplicate(current.messageId).subscribe({
      next: () => this.startPolling(current.messageId),
      error: () => this.error.set('Não foi possível publicar a duplicata.'),
    });
  }

  refreshQueues(): void {
    this.api.queues().subscribe({
      next: status => this.queues.set(status),
      error: () => this.error.set('Não foi possível consultar as filas.'),
    });
  }

  private startPolling(messageId: string): void {
    this.stopPolling();
    this.load(messageId);
    this.pollHandle = setInterval(() => this.load(messageId), 1000);
  }

  private load(messageId: string): void {
    this.api.find(messageId).subscribe({
      next: response => {
        this.command.set(response.command);
        this.refreshQueues();
        if (response.command.status === 'COMPLETED' || response.command.attempts >= 3) this.stopPolling();
      },
      error: () => this.error.set('Não foi possível atualizar o estado.'),
    });
  }

  private stopPolling(): void {
    if (this.pollHandle) clearInterval(this.pollHandle);
    this.pollHandle = undefined;
  }
}
